package com.innowise.order_service.kafka.config;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KafkaConfigVars {
    public static final String ORDER_TOPIC = "order.created";
    public static final String PAYMENT_TOPIC = "payment.created";

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value(value = "${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private String trustedPackages;

}
