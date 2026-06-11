package com.innowise.order_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderItemRequest {
    @NotEmpty(message = "Order must contains items")
    private List<OrderItemRequest> items;
}
