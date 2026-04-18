package com.arka.inventory.domain.inventorybalance.valueobject;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;

public record ReservationId(String value) {

    public ReservationId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("reservationId is required");
        }
        value = value.trim();
    }

    public static ReservationId of(String value) {
        return new ReservationId(value);
    }
}
