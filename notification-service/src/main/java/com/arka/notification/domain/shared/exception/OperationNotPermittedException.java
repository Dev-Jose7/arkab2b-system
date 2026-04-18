package com.arka.notification.domain.shared.exception;

public class OperationNotPermittedException extends DomainException {

    public OperationNotPermittedException(String errorCode, String message) {
        super(errorCode, message);
    }
}
