package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record ProductId(String value) {

    public ProductId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("product_id_invalido", "productId es obligatorio");
        }
        value = value.trim();
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }

    public static ProductId newId() {
        return new ProductId(UUID.randomUUID().toString());
    }
}
