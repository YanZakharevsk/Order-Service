package com.innowise.order_service.service.filter;

import com.innowise.order_service.jpa.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public record OrderFilter (LocalDate startCreationDate, LocalDate finishedCreationDate, List<OrderStatus> statuses) {
}
