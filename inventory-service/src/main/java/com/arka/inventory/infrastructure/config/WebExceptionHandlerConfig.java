package com.arka.inventory.infrastructure.config;

import com.arka.inventory.application.exception.ApplicationException;
import com.arka.inventory.application.exception.InventoryResourceNotFoundException;
import com.arka.inventory.domain.shared.exception.DomainException;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import com.arka.inventory.domain.shared.exception.OperationNotPermittedException;
import com.arka.inventory.infrastructure.adapter.in.web.response.ErrorResponse;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class WebExceptionHandlerConfig {
    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandlerConfig.class);

    @ExceptionHandler(InventoryResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(InventoryResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler({DomainInvariantViolationException.class, WebExchangeBindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        String message = ex instanceof WebExchangeBindException bindException
                ? bindException.getAllErrors().stream().findFirst().map(error -> error.getDefaultMessage()).orElse(ex.getMessage())
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, "inventory_validation_error", message);
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled inventory exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "inventory_internal_error", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now()));
    }
}
