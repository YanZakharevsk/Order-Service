package com.innowise.order_service.dto.response;

import com.innowise.order_service.dto.request.OrderItemRequest;
import com.innowise.order_service.jpa.entity.OrderItem;
import com.innowise.order_service.jpa.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderResponse {

    private Long orderId;
    private List<OrderItemResponse> orderItems;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private boolean deleted;
    private UserResponse user;

}
