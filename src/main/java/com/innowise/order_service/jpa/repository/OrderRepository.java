package com.innowise.order_service.jpa.repository;

import com.innowise.order_service.jpa.entity.Order;
import com.innowise.order_service.jpa.enums.OrderStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    
    Optional<Order> findById(Long id);

    @Query("""
        SELECT o
        FROM Order o
        WHERE o.userId = ?
""")
    List<Order> findByUserId(Long userId);

    List<Order> findAll(Specification<Order> specification, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);
}
