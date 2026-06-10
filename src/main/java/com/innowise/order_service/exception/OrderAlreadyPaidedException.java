package com.innowise.order_service.exception;

import org.springframework.http.HttpStatus;

public class OrderAlreadyPaidedException extends BaseException {
    public OrderAlreadyPaidedException(Long orderId)
    {
        super("Order with id " + orderId + " already paid for", "ORDER_ALREADY_PAID", HttpStatus.CONFLICT);
    }
}
