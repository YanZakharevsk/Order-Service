package com.innowise.order_service.kafka.dto;

import com.innowise.order_service.jpa.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PaymentKafkaDto {

    @NotNull(message = "CreatePayment ID can not be null")
    private String paymentId;

    @NotNull(message = "CreatePayment order ID can not be null")
    private Long orderId;

    @NotNull(message = "CreatePayment user ID can not be null")
    private Long userId;

    @NotNull(message = "CreatePayment status can not be null")
    private PaymentStatus paymentStatus;
}
