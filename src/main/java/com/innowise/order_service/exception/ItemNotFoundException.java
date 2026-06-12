package com.innowise.order_service.exception;

import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends BaseException {
    public ItemNotFoundException(Long itemId) {
        super("Item with id " + itemId + " not found", "ITEM_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
