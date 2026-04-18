package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record VariantId(String value) {

    public VariantId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("variant_id_invalido", "variantId es obligatorio");
        }
        value = value.trim();
    }

    public static VariantId of(String value) {
        return new VariantId(value);
    }

    public static VariantId newId() {
        return new VariantId(UUID.randomUUID().toString());
    }
}
