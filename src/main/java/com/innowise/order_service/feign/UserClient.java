package com.innowise.order_service.feign;

import com.innowise.order_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${user.service.url}",
        fallback = UserClientFallback.class
)
public interface UserClient {

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable(name = "id") Long userId);

}
