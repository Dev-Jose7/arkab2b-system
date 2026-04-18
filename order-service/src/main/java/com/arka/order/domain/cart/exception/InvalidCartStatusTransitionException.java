package com.arka.order.domain.cart.exception;

public class InvalidCartStatusTransitionException extends CartDomainException {

    public InvalidCartStatusTransitionException(String message) {
        super("transicion_estado_carrito_invalida", message);
    }
}
