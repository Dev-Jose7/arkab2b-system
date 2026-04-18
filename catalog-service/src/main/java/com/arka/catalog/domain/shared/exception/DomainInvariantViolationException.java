package com.arka.catalog.domain.shared.exception;

public class DomainInvariantViolationException extends DomainException {

    public DomainInvariantViolationException(String errorCode, String message) {
        super(errorCode, message);
    }
}
