package com.arka.order.domain.order.exception;

public class InvalidOrderTransitionException extends OrderDomainException {

    public InvalidOrderTransitionException(String message) {
        super("transicion_estado_invalida", message);
    }
}
