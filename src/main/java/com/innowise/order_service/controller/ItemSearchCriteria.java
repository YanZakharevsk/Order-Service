package com.innowise.order_service.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemSearchCriteria {
    private String name;
    private BigDecimal startPrice;
    private BigDecimal finishPrice;
    @Min(value = 0,message = "Page number can not less than 0")
    private int page = 0;
    @Min(value = 1, message = "Size number can not less than 1")
    @Max(value = 100, message = "Size number can not more than 100")
    private int size = 10;
}
