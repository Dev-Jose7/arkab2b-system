package com.arka.order.domain.cart.exception;

import com.arka.order.domain.shared.exception.DomainException;

public class CartDomainException extends DomainException {

    public CartDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}
