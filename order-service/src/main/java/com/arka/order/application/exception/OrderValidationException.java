package com.arka.order.application.exception;

public class OrderValidationException extends ApplicationException {

    public OrderValidationException(String message) {
        super("order_validation_error", message);
    }
}
