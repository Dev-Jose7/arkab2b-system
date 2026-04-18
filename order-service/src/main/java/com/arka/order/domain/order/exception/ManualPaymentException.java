package com.arka.order.domain.order.exception;

public class ManualPaymentException extends OrderDomainException {

    public ManualPaymentException(String message) {
        super("pago_manual_invalido", message);
    }
}
