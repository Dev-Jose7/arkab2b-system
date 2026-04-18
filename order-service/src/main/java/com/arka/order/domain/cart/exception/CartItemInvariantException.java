package com.arka.order.domain.cart.exception;

public class CartItemInvariantException extends CartDomainException {

    public CartItemInvariantException(String message) {
        super("invariante_item_carrito_invalida", message);
    }
}
