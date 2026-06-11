package com.innowise.order_service.service.specification;

import com.innowise.order_service.jpa.entity.Item;
import com.innowise.order_service.service.filter.ItemFilter;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ItemSpecification {

    public static final String NAME = "name";
    public static final String PRICE = "price";

    public ItemSpecification() {
    }

    public static Specification<Item> filterBy(ItemFilter itemFilter){

        if (itemFilter == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        return Specification
                .where(hasName(itemFilter.name()))
                .and(starWithPrice(itemFilter.startPrice()))
                .and(finishWithPrice(itemFilter.finishPrice()));
    }

    private static Specification<Item> hasName(String name) {
        return(
                (root, query, criteriaBuilder) ->
                        name == null || name.isBlank() ? criteriaBuilder.conjunction()
                                : criteriaBuilder.equal(root.get(NAME), name));
    }

    private static Specification<Item> starWithPrice(BigDecimal startPrice) {
        return (
                (root, query, criteriaBuilder) ->
                        startPrice == null ? criteriaBuilder.conjunction()
                                : criteriaBuilder.greaterThanOrEqualTo(root.get(PRICE), startPrice));
    }

    private static Specification<Item> finishWithPrice(BigDecimal finalPrice) {
        return (
                (root, query, criteriaBuilder) ->
                        finalPrice == null ? criteriaBuilder.conjunction()
                                : criteriaBuilder.lessThanOrEqualTo(root.get(PRICE), finalPrice));
    }
}
