package com.innowise.order_service.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.innowise.order_service.jpa.enums.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderSearchCriteria {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startCreationDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate finishedCreationDate;
    private List<OrderStatus> statuses;
    @Min(value = 0,message = "Page number can not less than 0")
    private int page = 0;
    @Min(value = 1, message = "Size number can not less than 1")
    @Max(value = 100, message = "Size number can not more than 100")
    private int size = 10;
}
