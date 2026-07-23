package com.innowise.order_service.kafka.config;

import com.innowise.order_service.kafka.dto.PaymentKafkaDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaConfigVars vars;

    @Bean
    public ConsumerFactory<String, PaymentKafkaDto> paymentConsumerFactory() {

        return new DefaultKafkaConsumerFactory<>(
                commonConsumerProps(),
                new StringDeserializer(),
                paymentDeserializer()
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentKafkaDto>
    paymentCreatedListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, PaymentKafkaDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory());
        return factory;
    }

    private Map<String, Object> commonConsumerProps() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, vars.getBootstrapAddress(),
                ConsumerConfig.GROUP_ID_CONFIG, vars.getGroupId(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
        );
    }

    private JsonDeserializer<PaymentKafkaDto> paymentDeserializer() {
        JsonDeserializer<PaymentKafkaDto> deserializer =
                new JsonDeserializer<>(PaymentKafkaDto.class);
        deserializer.addTrustedPackages(vars.getTrustedPackages());
        deserializer.ignoreTypeHeaders();
        return deserializer;
    }

}
