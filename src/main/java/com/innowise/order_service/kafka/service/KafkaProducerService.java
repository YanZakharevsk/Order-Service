package com.innowise.order_service.kafka.service;

import com.innowise.order_service.kafka.config.KafkaConfigVars;
import com.innowise.order_service.kafka.dto.OrderKafkaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    private static final String ORDER_TOPIC = KafkaConfigVars.ORDER_TOPIC;

    private final KafkaTemplate<String, OrderKafkaDto> kafkaTemplate;

    public void sendOrder(OrderKafkaDto orderKafkaDto){
        kafkaTemplate.send(ORDER_TOPIC, orderKafkaDto).whenComplete((result, throwable) ->
                logKafkaResult(throwable, ORDER_TOPIC, orderKafkaDto, result));
    }

    private void logKafkaResult(Throwable throwable, String topic, OrderKafkaDto message, SendResult<String, OrderKafkaDto> result) {
        if(throwable == null){
            log.info(
                    "TOPIC: {}. Sent message=[{}] with offset={}",
                    topic,
                    message,
                    result.getRecordMetadata().offset()
            );
        }else{
            log.error(
                    "TOPIC: {}. Unable to send message=[{}] due to: {}",
                    topic,
                    message,
                    throwable.getMessage(),
                    throwable
            );
        }
    }
}
