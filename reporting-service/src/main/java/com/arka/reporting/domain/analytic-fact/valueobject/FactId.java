package com.arka.reporting.domain.analyticfact.valueobject;

import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record FactId(String value) {

    public FactId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("fact_id_invalido", "factId es obligatorio");
        }
        value = value.trim();
    }

    public static FactId of(String value) {
        return new FactId(value);
    }

    public static FactId newId() {
        return new FactId(UUID.randomUUID().toString());
    }
}
