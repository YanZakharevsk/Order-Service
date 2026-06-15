package com.innowise.order_service.service.impl;

import com.innowise.order_service.dto.mapper.OrderMapper;
import com.innowise.order_service.dto.request.CreateOrderRequest;
import com.innowise.order_service.dto.request.OrderItemRequest;
import com.innowise.order_service.dto.request.UpdateOrderStatusRequest;
import com.innowise.order_service.dto.response.OrderResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.dto.response.UserResponse;
import com.innowise.order_service.exception.ItemNotFoundException;
import com.innowise.order_service.exception.OrderNotFoundException;
import com.innowise.order_service.feign.UserClient;
import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.jpa.entity.Order;
import com.innowise.order_service.jpa.enums.OrderStatus;
import com.innowise.order_service.jpa.repository.ItemRepository;
import com.innowise.order_service.jpa.repository.OrderRepository;
import com.innowise.order_service.service.filter.OrderFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserClient userClient;
    @Mock private ItemRepository itemRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks private OrderServiceImpl orderService;

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityRole(String role) {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().doReturn(List.of(new SimpleGrantedAuthority(role))).when(authentication).getAuthorities();
    }



    @Test
    @DisplayName("Create Order - Item Not Found Throws Exception")
    void createOrder_ItemNotFound_ThrowsException() {
        Long userId = 1L;
        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(10L);
        request.setOrderItems(List.of(itemRequest));

        Order order = new Order();

        when(orderMapper.toEntity(request)).thenReturn(order);
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> orderService.createOrder(userId, request));
    }

    @Test
    @DisplayName("Get Order By Id - Admin View Any Order")
    void getOrderById_AsAdmin_Success() {
        Long orderId = 100L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(2L);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(2L);

        mockSecurityRole("ROLE_ADMIN");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userResponse));

        OrderResponse response = orderService.getOrderById(1L, orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
    }

    @Test
    @DisplayName("Get Order By Id - User View Own Order")
    void getOrderById_AsUser_OwnOrder_Success() {
        Long orderId = 100L;
        Long userId = 2L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);

        mockSecurityRole("ROLE_USER");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));

        OrderResponse response = orderService.getOrderById(userId, orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
    }

    @Test
    @DisplayName("Get Order By Id - Not Found")
    void getOrderById_NotFound_ThrowsOrderNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L, 1L));
    }

    @Test
    @DisplayName("Get Orders By User Id - Success")
    void getOrdersByUserId_Success() {
        Long userId = 1L;
        Order order = new Order();
        order.setId(200L);
        order.setUserId(userId);

        UserResponse userResponse = new UserResponse();

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order));
        when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));

        List<OrderResponse> result = orderService.getOrdersByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).getOrderId());
    }

    @Test
    @DisplayName("Get All Orders - Success")
    void getAllOrders_Success() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<OrderStatus> statuses = List.of(OrderStatus.CREATED, OrderStatus.PAID);

        OrderFilter filter = new OrderFilter(startDate, endDate, statuses);

        Order order = new Order();
        order.setId(300L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);

        Page<Order> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);

        UserResponse userResponse = new UserResponse();

        mockSecurityRole("ROLE_ADMIN");
        when(orderRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(userClient.getUserById(1L)).thenReturn(ResponseEntity.ok(userResponse));

        PageResponse<OrderResponse> response = orderService.getAllOrders(filter, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(300L, response.getContent().get(0).getOrderId());
    }

    @Test
    @DisplayName("Update Order Status - Success")
    void updateOrderStatusById_Success() {
        Long orderId = 100L;
        Long userId = 1L;
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAID);

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        UserResponse userResponse = new UserResponse();

        mockSecurityRole("ROLE_USER");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.updateStatus(order, request)).thenReturn(order);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));

        OrderResponse response = orderService.updateOrderStatusById(userId, orderId, request);

        assertNotNull(response);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Update Order Items - Success")
    void updateOrderItemById_Success() {
        Long orderId = 100L;
        Long userId = 1L;

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemId(10L);
        itemReq.setQuantity(5);
        request.setOrderItems(List.of(itemReq));

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderItems(new ArrayList<>());

        Item item = new Item(10L, "Product", BigDecimal.TEN);
        UserResponse userResponse = new UserResponse();

        mockSecurityRole("ROLE_USER");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));


        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));

        OrderResponse response = orderService.updateOrderItemById(userId, orderId, request);

        assertNotNull(response);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Delete Order By Id - Success")
    void deleteOrderById_Success() {
        Long orderId = 100L;
        Long userId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        mockSecurityRole("ROLE_USER");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(userId, orderId);

        verify(orderRepository, times(1)).delete(order);
    }
}