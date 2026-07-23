package com.innowise.order_service.controller;

import com.innowise.order_service.dto.mapper.OrderMapper;
import com.innowise.order_service.dto.request.CreateOrderRequest;
import com.innowise.order_service.dto.request.UpdateOrderStatusRequest;
import com.innowise.order_service.dto.response.OrderResponse;
import com.innowise.order_service.dto.response.PageResponse;
import com.innowise.order_service.jpa.enums.OrderStatus;
import com.innowise.order_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController()
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal Long currentUserId, @Valid @RequestBody CreateOrderRequest request){
        OrderResponse orderResponse = orderService.createOrder(currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderResponse);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @orderServiceImpl.isOrderOwner(#orderId, authentication.principal)")
    public ResponseEntity<OrderResponse> getOrderById(@AuthenticationPrincipal Long userId, @PathVariable(name = "id") Long orderId){
        OrderResponse orderResponse = orderService.getOrderById(userId, orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderResponse);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@AuthenticationPrincipal Long currentUserId){
        List<OrderResponse> orderResponses = orderService.getOrdersByUserId(currentUserId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderResponses);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(required = false) LocalDate startCreationDate,
            @RequestParam(required = false) LocalDate finishedCreationDate,
            @RequestParam(required = false)List<OrderStatus> statuses,
            @Min(value = 0,message = "Page number can not less than 0")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Size number can not less than 1")
            @Max(value = 100, message = "Size number can not more than 100")
            @RequestParam(defaultValue = "10") int size){

        OrderSearchCriteria criteria = new OrderSearchCriteria();
        criteria.setStartCreationDate(startCreationDate);
        criteria.setFinishedCreationDate(finishedCreationDate);
        criteria.setStatuses(statuses);
        criteria.setSize(size);
        criteria.setPage(page);

        PageResponse<OrderResponse> pageResponse = orderService.getAllOrders(orderMapper.toFilter(criteria), criteria.getPage(), criteria.getSize());

        return ResponseEntity.status(HttpStatus.OK)
                .body(pageResponse);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@AuthenticationPrincipal Long userId, @PathVariable(name = "id") Long orderId, @Valid @RequestBody UpdateOrderStatusRequest request){
        OrderResponse orderResponse = orderService.updateOrderStatusById(userId, orderId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @orderServiceImpl.isOrderOwner(#orderId, authentication.principal)")
    public ResponseEntity<OrderResponse> updateOrderItemById(@AuthenticationPrincipal Long userId, @PathVariable(name = "id") Long orderId, @Valid @RequestBody CreateOrderRequest request){
        OrderResponse orderResponse = orderService.updateOrderItemById(userId,orderId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(orderResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @orderServiceImpl.isOrderOwner(#orderId, authentication.principal)")
    public ResponseEntity<Void> deleteOrder(@AuthenticationPrincipal Long userId, @PathVariable(name = "id") Long orderId){
        orderService.deleteOrderById(userId, orderId);

        return ResponseEntity.noContent().build();
    }



}
