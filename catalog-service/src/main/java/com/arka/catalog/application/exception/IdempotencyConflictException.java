package com.arka.catalog.application.exception;

public class IdempotencyConflictException extends ApplicationException {

    public IdempotencyConflictException() {
        super("idempotencia_conflicto", "La Idempotency-Key ya existe con un payload diferente");
    }
}
