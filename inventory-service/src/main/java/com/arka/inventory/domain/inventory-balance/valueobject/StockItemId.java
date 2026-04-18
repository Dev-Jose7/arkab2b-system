package com.arka.inventory.domain.inventorybalance.valueobject;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;

public record StockItemId(String value) {

    public StockItemId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("stockItemId is required");
        }
        value = value.trim();
    }

    public static StockItemId of(String value) {
        return new StockItemId(value);
    }
}
