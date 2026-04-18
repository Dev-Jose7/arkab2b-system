package com.arka.inventory.domain.inventorybalance.entity;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record ReservationLedger(
        String ledgerId,
        String tenantId,
        String reservationId,
        String entryType,
        int qty,
        String note,
        Instant createdAt) {

    public ReservationLedger {
        requireNotBlank(ledgerId, "ledgerId");
        requireNotBlank(tenantId, "tenantId");
        requireNotBlank(reservationId, "reservationId");
        requireNotBlank(entryType, "entryType");
        if (qty <= 0) {
            throw new DomainInvariantViolationException("qty must be positive");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
