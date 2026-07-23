package com.innowise.order_service.kafka.config;

import com.innowise.order_service.kafka.dto.OrderKafkaDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaConfigVars vars;

    @Bean
    public ProducerFactory<String, OrderKafkaDto> orderProducerFactory() {
        return createProducerFactory();
    }

    @Bean
    public KafkaTemplate<String, OrderKafkaDto> orderKafkaTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }

    private <T> ProducerFactory<String, T> createProducerFactory() {
        Map<String, Object> configProps = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, vars.getBootstrapAddress(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
