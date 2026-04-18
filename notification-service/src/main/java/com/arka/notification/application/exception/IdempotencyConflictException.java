package com.arka.notification.application.exception;

public class IdempotencyConflictException extends ApplicationException {

    public IdempotencyConflictException() {
        super(
                "conflicto_idempotencia",
                "Idempotency-Key ya usada con un payload distinto");
    }
}
