package com.arka.order.application.exception;

public class OrderNotFoundException extends ApplicationException {

    public OrderNotFoundException(String message) {
        super("order_resource_not_found", message);
    }
}
