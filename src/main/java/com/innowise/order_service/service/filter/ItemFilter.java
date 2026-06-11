package com.innowise.order_service.service.filter;

import java.math.BigDecimal;

public record ItemFilter (String name, BigDecimal startPrice, BigDecimal finishPrice) {
}
