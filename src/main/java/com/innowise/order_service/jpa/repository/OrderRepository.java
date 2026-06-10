package com.innowise.order_service.jpa.repository;

import com.innowise.order_service.jpa.entity.Order;
import com.innowise.order_service.jpa.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    
    Optional<Order> findById(Long id);

    @Query("""
        SELECT o
        FROM Order o
        WHERE o.userId = :userId
""")
    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    boolean existsByIdAndUserId(Long orderId, Long userId);
}
