package com.arka.directory.infrastructure.config;

import com.arka.directory.application.exception.ApplicationException;
import com.arka.directory.application.exception.DirectoryResourceNotFoundException;
import com.arka.directory.domain.shared.exception.DomainException;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import com.arka.directory.domain.shared.exception.OperationNotPermittedException;
import com.arka.directory.infrastructure.adapter.in.web.response.ErrorResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class WebExceptionHandlerConfig {

    @ExceptionHandler(DirectoryResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DirectoryResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.errorCode(), ex.getMessage());
    }

    @ExceptionHandler({DomainInvariantViolationException.class, WebExchangeBindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        String message = ex instanceof WebExchangeBindException bindException
                ? bindException.getAllErrors().stream().findFirst().map(error -> error.getDefaultMessage()).orElse(ex.getMessage())
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, "directory_validation_error", message);
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
        return build(HttpStatus.FORBIDDEN, "directory_access_denied", "Access Denied");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "directory_internal_error", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now()));
    }
}
