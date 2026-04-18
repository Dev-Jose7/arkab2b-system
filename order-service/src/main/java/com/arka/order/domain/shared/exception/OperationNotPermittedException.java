package com.arka.order.domain.shared.exception;

public class OperationNotPermittedException extends DomainException {

    public OperationNotPermittedException(String message) {
        super("operacion_no_permitida", message);
    }
}
