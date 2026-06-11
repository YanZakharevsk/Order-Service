package com.innowise.order_service.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ItemResponse {
    private Long itemId;
    private String name;
    private BigDecimal price;
}
