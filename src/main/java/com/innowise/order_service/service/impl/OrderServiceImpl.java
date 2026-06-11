package com.innowise.order_service.service.impl;

import com.innowise.order_service.dto.mapper.OrderMapper;
import com.innowise.order_service.dto.request.CreateOrderRequest;
import com.innowise.order_service.dto.request.UpdateOrderStatusRequest;
import com.innowise.order_service.dto.response.OrderItemResponse;
import com.innowise.order_service.dto.response.OrderResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.dto.response.UserResponse;
import com.innowise.order_service.exception.ItemNotFoundException;
import com.innowise.order_service.exception.OrderAlreadyPaidedException;
import com.innowise.order_service.exception.OrderNotFoundException;
import com.innowise.order_service.feign.UserClient;
import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.jpa.entity.Order;
import com.innowise.order_service.jpa.entity.OrderItem;
import com.innowise.order_service.jpa.enums.OrderStatus;
import com.innowise.order_service.jpa.repository.ItemRepository;
import com.innowise.order_service.jpa.repository.OrderRepository;
import com.innowise.order_service.kafka.events.OrderCreatedEvent;
import com.innowise.order_service.service.OrderService;
import com.innowise.order_service.service.filter.OrderFilter;
import com.innowise.order_service.service.specification.OrderSpecification;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository, UserClient userClient, ItemRepository itemRepository, OrderMapper orderMapper, ApplicationEventPublisher applicationEventPublisher) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.itemRepository = itemRepository;
        this.orderMapper = orderMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {

        Order order =  orderMapper.toEntity(request);
        order.setStatus(OrderStatus.CREATED);
        order.setUserId(userId);
        order.setOrderItems(addOrderItems(request,order));
        order.setTotalPrice(calculateOrderTotalPrice(order));

        order = orderRepository.save(order);
        OrderResponse response = createOrderResponse(order, null);
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(response));

        return response;
    }

    @Override
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        return createOrderResponse(order, null);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        UserResponse response = getUserInfoByUserId(userId);

        return orders.stream().map(order -> createOrderResponse(order, response)).toList();
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(OrderFilter filter, int page, int size) {
        Specification<Order> specification = OrderSpecification.filterBy(filter);

        Page<Order> orderPage = orderRepository.findAll(
                specification,
                PageRequest.of(page, size));

        List<OrderResponse> content = orderPage.getContent()
                .stream()
                .map(order -> createOrderResponse(order, null))
                .toList();

        return new PageResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements()
        );
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusById(Long userId, Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        Order updatedOrder = orderMapper.updateStatus(order, request);
        updatedOrder = orderRepository.save(updatedOrder);

        return createOrderResponse(updatedOrder, null);
    }

    @Transactional
    public void internalUpdateOrderStatus(Long userId, Long orderId, UpdateOrderStatusRequest request){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        Order updatedOrder = orderMapper.updateStatus(order, request);
        updatedOrder = orderRepository.save(updatedOrder);
    }



    @Override
    @Transactional
    public OrderResponse updateOrderItemById(Long userId, Long orderId, CreateOrderRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        if(order.getStatus().equals(OrderStatus.CREATED)) {
            order.getOrderItems().clear();
            order.getOrderItems().addAll(addOrderItems(request, order));
            order.setTotalPrice(calculateOrderTotalPrice(order));
            orderRepository.save(order);
        }
        else{
            throw new OrderAlreadyPaidedException(orderId);
        }

        OrderResponse response = createOrderResponse(order, null);
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(response));

        return response;
    }

    @Override
    @Transactional
    public void deleteOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        orderRepository.delete(order);
    }

    private List<OrderItem> addOrderItems(CreateOrderRequest request, Order order){

        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(req -> {
                    Item item = itemRepository.findById(req.getItemId()).orElseThrow(() -> new ItemNotFoundException(req.getItemId()));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setItem(item);
                    orderItem.setQuantity(req.getQuantity());
                    return orderItem;
                }).collect(Collectors.toList());
        return orderItems;
    }

    private BigDecimal calculateOrderTotalPrice(Order order) {

        BigDecimal totalPrice = new BigDecimal(0);
        List<OrderItem>  orderItems = order.getOrderItems();

        for(OrderItem orderItem : orderItems){
            totalPrice = totalPrice.add(orderItem.getItem().getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
        return totalPrice;
    }

    private OrderResponse createOrderResponse(Order order, UserResponse response){
        if(response == null) {
            response = getUserInfoByUserId(order.getUserId());
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(order.getId());
        orderResponse.setTotalPrice(order.getTotalPrice());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setDeleted(order.isDeleted());

        orderResponse.setOrderItems(order.getOrderItems().stream()
                .map(item -> {
                    OrderItemResponse r = new OrderItemResponse();
                    r.setItemId(item.getItem().getId());
                    r.setQuantity(item.getQuantity());
                    r.setItemName(item.getItem().getName());
                    r.setPrice(item.getItem().getPrice());
                    return r;
                }).toList());

        orderResponse.setUser(response);

        return orderResponse;
    }

    private UserResponse getUserInfoByUserId(Long userId){
        ResponseEntity<UserResponse> responseEntity = userClient.getUserById(userId);

        return responseEntity.getBody();
    }

    @Transactional
    public boolean isOrderOwner(Long orderId, Long userId){
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }
}
