package com.innowise.order_service.dto.request;

import com.innowise.order_service.jpa.enums.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "Order must contains items")
    private List<OrderItemRequest> orderItems;
}
