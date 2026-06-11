package com.innowise.order_service.feign;

import com.innowise.order_service.dto.response.UserResponse;
import com.innowise.order_service.exception.UserServiceUnavailableException;
<<<<<<< HEAD
=======
import org.springframework.http.HttpStatus;
>>>>>>> 57d6463 (UserClientFallback was added for pattern realization: 'curcuit breaker')
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {
<<<<<<< HEAD
=======
    @Override
    public ResponseEntity<UserResponse> getCurrentUser() {
        throw new UserServiceUnavailableException();
    }
>>>>>>> 57d6463 (UserClientFallback was added for pattern realization: 'curcuit breaker')

    @Override
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        throw new UserServiceUnavailableException();
    }
}
