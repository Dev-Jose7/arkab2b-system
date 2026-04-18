package com.arka.inventory.domain.inventorybalance.valueobject;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;

public record WarehouseId(String value) {

    public WarehouseId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("warehouseId is required");
        }
        value = value.trim();
    }

    public static WarehouseId of(String value) {
        return new WarehouseId(value);
    }
}
