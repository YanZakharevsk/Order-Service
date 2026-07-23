package com.innowise.order_service.kafka.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrderKafkaDto {

    @NotNull(message = "CreateOrder ID can not be null.")
    private Long orderId;

    @NotNull(message = "CreateOrder user ID can not be null.")
    private Long userId;

    public OrderKafkaDto(Long orderId, Long userId, BigDecimal paymentAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentAmount = paymentAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount.setScale(2, RoundingMode.HALF_UP);
    }

    @NotNull(message = "CreateOrder amount can not be null.")
    private BigDecimal paymentAmount;

}
