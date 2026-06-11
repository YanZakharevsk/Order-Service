package com.innowise.order_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateItemRequest {
    @NotBlank(message = "Name can not be blank")
    @Size(min = 3, max = 100, message = "The length of the name must be 3 and 100 symbols")
    private String name;

    @NotNull(message = "Price can not be empty")
    @Positive(message = "Prise must be positive")
    @Min(0)
    private BigDecimal price;
}

