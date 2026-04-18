package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainInvariantViolationException("precio_invalido", "El monto debe ser mayor que cero");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainInvariantViolationException("precio_invalido", "La moneda es obligatoria");
        }
        currency = currency.trim().toUpperCase();
        if (currency.length() != 3) {
            throw new DomainInvariantViolationException("precio_invalido", "La moneda debe tener 3 caracteres");
        }
        amount = amount.stripTrailingZeros();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
}
