package com.innowise.order_service.exception;

import org.springframework.http.HttpStatus;

public class UserServiceUnavailableException extends BaseException {
    public UserServiceUnavailableException() {
        super("User service is not responding. Please try again", "USER_SERVICE_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
