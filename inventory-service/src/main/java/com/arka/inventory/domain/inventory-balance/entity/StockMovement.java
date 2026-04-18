package com.arka.inventory.domain.inventorybalance.entity;

import com.arka.inventory.domain.inventorybalance.enumtype.StockMovementType;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record StockMovement(
        String movementId,
        String organizationId,
        String stockItemId,
        String warehouseId,
        String sku,
        StockMovementType movementType,
        int deltaQty,
        String reason,
        String reservationId,
        String orderId,
        String correlationId,
        Instant createdAt) {

    public StockMovement {
        requireNotBlank(movementId, "movementId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(stockItemId, "stockItemId");
        requireNotBlank(warehouseId, "warehouseId");
        requireNotBlank(sku, "sku");
        requireNotBlank(reason, "reason");
        if (movementType == null) {
            throw new DomainInvariantViolationException("movementType is required");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
