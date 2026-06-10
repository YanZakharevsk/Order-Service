package com.innowise.order_service.jpa.repository;

import com.innowise.order_service.jpa.entity.Item;
import org.hibernate.boot.models.JpaAnnotations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
