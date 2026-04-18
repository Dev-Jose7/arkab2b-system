package com.arka.reporting.domain.analyticfact.valueobject;

import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;

public record SourceEventId(String value) {

    public SourceEventId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("source_event_id_invalido", "sourceEventId es obligatorio");
        }
        value = value.trim();
    }

    public static SourceEventId of(String value) {
        return new SourceEventId(value);
    }
}
