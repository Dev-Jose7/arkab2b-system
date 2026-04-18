package com.arka.order.application.exception;

public class OrderConflictException extends ApplicationException {

    public OrderConflictException(String message) {
        super("order_conflict", message);
    }
}
