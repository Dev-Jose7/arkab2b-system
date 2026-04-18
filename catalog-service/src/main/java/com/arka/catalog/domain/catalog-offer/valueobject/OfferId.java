package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record OfferId(String value) {

    public OfferId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("offer_id_invalido", "offerId es obligatorio");
        }
        value = value.trim();
    }

    public static OfferId of(String value) {
        return new OfferId(value);
    }

    public static OfferId newId() {
        return new OfferId(UUID.randomUUID().toString());
    }
}
