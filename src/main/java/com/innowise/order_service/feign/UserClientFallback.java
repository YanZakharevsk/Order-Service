package com.innowise.order_service.feign;

import com.innowise.order_service.dto.response.UserResponse;
import com.innowise.order_service.exception.UserServiceUnavailableException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        throw new UserServiceUnavailableException();
    }
}
