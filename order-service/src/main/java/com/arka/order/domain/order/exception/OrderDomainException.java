package com.arka.order.domain.order.exception;

import com.arka.order.domain.shared.exception.DomainException;

public class OrderDomainException extends DomainException {

    public OrderDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}
