package com.innowise.order_service.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardShortResponse {
    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
}
