package com.arka.reporting.infrastructure.config;

import com.arka.reporting.application.exception.ApplicationException;
import com.arka.reporting.application.exception.ReportingResourceNotFoundException;
import com.arka.reporting.domain.shared.exception.DomainException;
import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;
import com.arka.reporting.domain.shared.exception.OperationNotPermittedException;
import com.arka.reporting.infrastructure.adapter.in.web.response.ErrorResponse;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class WebExceptionHandlerConfig {
    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandlerConfig.class);

    @ExceptionHandler(ReportingResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ReportingResourceNotFoundException ex) {
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
        return build(HttpStatus.BAD_REQUEST, "reporting_validation_error", message);
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "reporting_access_denied", "Access Denied");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String code = switch (status) {
            case UNAUTHORIZED -> "reporting_unauthorized";
            case FORBIDDEN -> "reporting_access_denied";
            case NOT_FOUND -> "reporting_resource_not_found";
            default -> "reporting_http_error";
        };
        return build(status, code, ex.getReason() == null ? ex.getMessage() : ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled reporting exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "reporting_internal_error", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now()));
    }
}
