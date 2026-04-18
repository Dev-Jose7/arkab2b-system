package com.arka.notification.infrastructure.config;

import com.arka.notification.application.exception.ApplicationException;
import com.arka.notification.application.exception.NotificationResourceNotFoundException;
import com.arka.notification.domain.shared.exception.DomainException;
import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;
import com.arka.notification.domain.shared.exception.OperationNotPermittedException;
import com.arka.notification.infrastructure.adapter.in.web.response.ErrorResponse;
import java.time.Instant;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class WebExceptionHandlerConfig {

    @ExceptionHandler(NotificationResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotificationResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler({DomainInvariantViolationException.class, WebExchangeBindException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        String message = ex instanceof WebExchangeBindException bindException
                ? bindException.getAllErrors().stream()
                        .findFirst()
                        .map(error -> error.getDefaultMessage())
                        .orElse(ex.getMessage())
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, "notification_validation_error", message);
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(OperationNotPermittedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateKeyException ex) {
        return build(HttpStatus.CONFLICT, "duplicate_key", ex.getMostSpecificCause().getMessage());
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
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "notification_internal_error", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now()));
    }
}
