package com.innowise.order_service.kafka.events;

import com.innowise.order_service.dto.mapper.OrderMapper;
import com.innowise.order_service.kafka.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    private final OrderMapper mapper;
    private final KafkaProducerService kafkaProducerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderToKafka(OrderCreatedEvent event){
        kafkaProducerService.sendOrder(
                mapper.responseToKafkaDto(event.getOrderResponse())
        );
    }
}
