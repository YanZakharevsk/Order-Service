package com.innowise.order_service.exception;

import com.innowise.order_service.dto.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidJwtTokenException(InvalidJwtTokenException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<ExceptionResponse> handleUserServiceUnavailableException(UserServiceUnavailableException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleItemNotFoundException(ItemNotFoundException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(OrderAlreadyPaidedException.class)
    public ResponseEntity<ExceptionResponse> handleOrderAlreadyPaidedException(OrderAlreadyPaidedException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest httpServletRequest){

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ExceptionResponse response = new ExceptionResponse();
        response.setErrorCode("VALIDATION_ERROR");
        response.setMessage(message);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setTimestamp(Instant.now());
        response.setPath(httpServletRequest.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(HttpServletRequest httpServletRequest){
        ExceptionResponse response = new ExceptionResponse(
                "AUTHENTICATION ERROR",
                "Authentication required",
                HttpStatus.UNAUTHORIZED.value(),
                Instant.now(),
                httpServletRequest.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(HttpServletRequest httpServletRequest){
        ExceptionResponse response = new ExceptionResponse(
                "AUTHORIZATION ERROR",
                "Access Denied",
                HttpStatus.FORBIDDEN.value(),
                Instant.now(),
                httpServletRequest.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<ExceptionResponse> createExceptionResponse(BaseException ex, HttpServletRequest httpServletRequest) {
        ExceptionResponse response = new ExceptionResponse();
        response.setErrorCode(ex.getErrorCode());
        response.setMessage(ex.getMessage());
        response.setStatus(ex.getStatus().value());
        response.setTimestamp(Instant.now());
        response.setPath(httpServletRequest.getRequestURI());
        return new ResponseEntity<>(response, ex.getStatus());
    }
}
