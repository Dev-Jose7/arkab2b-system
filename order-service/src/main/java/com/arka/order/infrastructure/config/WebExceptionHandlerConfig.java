package com.arka.order.infrastructure.config;

import com.arka.order.application.exception.ApplicationException;
import com.arka.order.application.exception.OrderNotFoundException;
import com.arka.order.domain.shared.exception.DomainException;
import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import com.arka.order.domain.shared.exception.OperationNotPermittedException;
import com.arka.order.infrastructure.adapter.in.web.response.ErrorResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class WebExceptionHandlerConfig {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler({DomainInvariantViolationException.class, WebExchangeBindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        String message = ex instanceof WebExchangeBindException bindException
                ? bindException.getAllErrors().stream().findFirst().map(error -> error.getDefaultMessage()).orElse(ex.getMessage())
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, "order_validation_error", message);
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(OperationNotPermittedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplication(ApplicationException ex) {
        return build(HttpStatus.CONFLICT, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        return build(HttpStatus.CONFLICT, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "order_access_denied", "Access Denied");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String code = switch (status) {
            case UNAUTHORIZED -> "order_unauthorized";
            case FORBIDDEN -> "order_access_denied";
            case NOT_FOUND -> "order_resource_not_found";
            default -> "order_http_error";
        };
        return build(status, code, ex.getReason() == null ? ex.getMessage() : ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "order_internal_error", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now()));
    }
}
