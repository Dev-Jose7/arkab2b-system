package com.arka.inventory.application.exception;

public class IdempotencyConflictException extends ApplicationException {

    public IdempotencyConflictException(String message) {
        super("idempotency_conflict", message);
    }
}
