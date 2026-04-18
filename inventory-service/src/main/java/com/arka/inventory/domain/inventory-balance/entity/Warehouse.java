package com.arka.inventory.domain.inventorybalance.entity;

import com.arka.inventory.domain.inventorybalance.enumtype.WarehouseStatus;
import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record Warehouse(
        String warehouseId,
        String organizationId,
        String code,
        String name,
        String countryCode,
        WarehouseStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public Warehouse {
        requireNotBlank(warehouseId, "warehouseId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(code, "code");
        requireNotBlank(name, "name");
        requireNotBlank(countryCode, "countryCode");
        status = status == null ? WarehouseStatus.ACTIVE : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
