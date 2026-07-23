package com.innowise.order_service.dto.request;

import com.innowise.order_service.jpa.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Order status can not be null")
    private String status;
}
