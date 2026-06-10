package com.innowise.order_service.jpa.repository;

import com.innowise.order_service.jpa.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
