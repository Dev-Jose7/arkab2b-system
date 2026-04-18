package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record PriceId(String value) {

    public PriceId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("price_id_invalido", "priceId es obligatorio");
        }
        value = value.trim();
    }

    public static PriceId of(String value) {
        return new PriceId(value);
    }

    public static PriceId newId() {
        return new PriceId(UUID.randomUUID().toString());
    }
}
