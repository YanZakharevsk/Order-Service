package com.innowise.order_service.kafka.events;

import com.innowise.order_service.dto.response.OrderResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {
    private final OrderResponse orderResponse;
}
