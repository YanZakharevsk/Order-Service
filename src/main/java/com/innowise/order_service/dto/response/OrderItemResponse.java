package com.innowise.order_service.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
}
