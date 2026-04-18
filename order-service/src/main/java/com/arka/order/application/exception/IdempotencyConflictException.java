package com.arka.order.application.exception;

public class IdempotencyConflictException extends ApplicationException {

    public IdempotencyConflictException(String message) {
        super("conflicto_idempotencia", message);
    }
}
