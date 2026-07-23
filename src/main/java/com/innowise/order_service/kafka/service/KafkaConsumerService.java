package com.innowise.order_service.kafka.service;

import com.innowise.order_service.dto.request.UpdateOrderStatusRequest;
import com.innowise.order_service.jpa.enums.OrderStatus;
import com.innowise.order_service.jpa.enums.PaymentStatus;
import com.innowise.order_service.kafka.config.KafkaConfigVars;
import com.innowise.order_service.kafka.dto.PaymentKafkaDto;
import com.innowise.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    private static final String PAYMENT_TOPIC = KafkaConfigVars.PAYMENT_TOPIC;

    private final OrderService orderService;

    @KafkaListener(
            topics = PAYMENT_TOPIC,
            autoStartup = "true",
            containerFactory = "paymentCreatedListenerContainerFactory"
    )
    public void listenPayment(
            @Payload PaymentKafkaDto paymentKafkaDto,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition){
        log.info("TOPIC: {}. Received message=[{}] from partition {}",
                PAYMENT_TOPIC, paymentKafkaDto, partition);

        if(paymentKafkaDto.getPaymentStatus().equals(PaymentStatus.SUCCESS)){
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAID);
            orderService.internalUpdateOrderStatus(paymentKafkaDto.getUserId(), paymentKafkaDto.getOrderId(), request);
        }else if(paymentKafkaDto.getPaymentStatus().equals(PaymentStatus.FAILED)){
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAYMENT_FAILED);
            orderService.internalUpdateOrderStatus(paymentKafkaDto.getUserId(), paymentKafkaDto.getOrderId(), request);
        }
    }
}
