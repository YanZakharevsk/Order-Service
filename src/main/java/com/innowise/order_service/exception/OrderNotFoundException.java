package com.innowise.order_service.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BaseException {
    public OrderNotFoundException(Long orderId) {
        super("Order with id " + orderId + " not found", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
