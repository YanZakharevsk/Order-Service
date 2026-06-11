package com.innowise.order_service.service.specification;

import com.innowise.order_service.jpa.entity.Order;
import com.innowise.order_service.jpa.enums.OrderStatus;
import com.innowise.order_service.service.filter.OrderFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecification {
    public static final String DATE = "createdAt";
    public static final String STATUS = "status";

    public OrderSpecification() {
    }

    public static Specification<Order> filterBy(OrderFilter orderFilter){
        if (orderFilter == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if(orderFilter.startCreationDate() != null){
            startDate = orderFilter.startCreationDate().atStartOfDay();
        }
        if(orderFilter.finishedCreationDate() != null) {
            endDate = orderFilter.finishedCreationDate().atStartOfDay();
        }
        return Specification
                .where(startWithDate(startDate))
                .and(finishedWithDate(endDate))
                .and(hasStatuses(orderFilter.statuses()));
    }

    private static Specification<Order> startWithDate(LocalDateTime startCreationDate) {
        return (
                (root, query, criteriaBuilder) ->
                        startCreationDate == null ? criteriaBuilder.conjunction()
                                : criteriaBuilder.greaterThanOrEqualTo(root.get(DATE), startCreationDate)
                );
    }

    private static Specification<Order> finishedWithDate(LocalDateTime finishedCreationDate) {
        return (
                (root, query, criteriaBuilder) ->
                        finishedCreationDate == null ? criteriaBuilder.conjunction()
                                : criteriaBuilder.lessThanOrEqualTo(root.get(DATE), finishedCreationDate)
        );
    }

    private static Specification<Order> hasStatuses(List<OrderStatus> statuses) {
        return (
                (root, query, criteriaBuilder) ->
                        statuses == null ? criteriaBuilder.conjunction()
                                : root.get(STATUS).in(statuses)
                );
    }

}
