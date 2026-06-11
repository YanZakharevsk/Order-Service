package com.innowise.order_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull(message = "Item id can not be null")
    private Long itemId;

    @NotNull(message = "Quantity can not be null")
    @Positive(message = "Quantity must be positive")
    @Max(value = 5, message = "Quantity of items can not more than 5")
    private Integer quantity;
}
