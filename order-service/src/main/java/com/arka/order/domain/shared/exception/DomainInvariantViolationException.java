package com.arka.order.domain.shared.exception;

public class DomainInvariantViolationException extends DomainException {

    public DomainInvariantViolationException(String message) {
        super("invariante_de_dominio_invalida", message);
    }
}
