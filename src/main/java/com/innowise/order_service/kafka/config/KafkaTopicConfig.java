package com.innowise.order_service.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    public static final String ORDER_TOPIC = KafkaConfigVars.ORDER_TOPIC;
    public static final String PAYMENT_TOPIC = KafkaConfigVars.PAYMENT_TOPIC;

    private final KafkaConfigVars vars;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, vars.getBootstrapAddress());
        return new KafkaAdmin(configs);
    }

    @Bean
    public Map<String, NewTopic> topics() {
        Map<String, NewTopic> topics = new HashMap<>();
        topics.put(ORDER_TOPIC, orderTopic());
        topics.put(PAYMENT_TOPIC, paymentTopic());
        return topics;
    }

    @Bean
    public NewTopic orderTopic() {
        return new NewTopic(ORDER_TOPIC, 1, (short) 1);
    }

    @Bean
    public NewTopic paymentTopic() {
        return new NewTopic(PAYMENT_TOPIC, 1, (short) 1);
    }
}
