package com.arka.order.domain.order.exception;

public class OrderConsistencyException extends OrderDomainException {

    public OrderConsistencyException(String message) {
        super("consistencia_pedido_invalida", message);
    }
}
