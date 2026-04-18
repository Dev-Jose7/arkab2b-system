package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record TimeWindow(Instant effectiveFrom, Instant effectiveUntil) {

    public TimeWindow {
        if (effectiveFrom == null) {
            throw new DomainInvariantViolationException("precio_invalido", "effectiveFrom es obligatorio");
        }
        if (effectiveUntil != null && !effectiveUntil.isAfter(effectiveFrom)) {
            throw new DomainInvariantViolationException(
                    "precio_invalido",
                    "effective_until debe ser mayor que effective_from");
        }
    }

    public static TimeWindow of(Instant effectiveFrom, Instant effectiveUntil) {
        return new TimeWindow(effectiveFrom, effectiveUntil);
    }

    public boolean contains(Instant instant) {
        if (instant == null) {
            return false;
        }
        boolean starts = !instant.isBefore(effectiveFrom);
        boolean ends = effectiveUntil == null || instant.isBefore(effectiveUntil);
        return starts && ends;
    }

    public boolean overlaps(TimeWindow other) {
        Instant thisEnd = effectiveUntil == null ? Instant.MAX : effectiveUntil;
        Instant otherEnd = other.effectiveUntil == null ? Instant.MAX : other.effectiveUntil;
        return effectiveFrom.isBefore(otherEnd) && other.effectiveFrom.isBefore(thisEnd);
    }
}
