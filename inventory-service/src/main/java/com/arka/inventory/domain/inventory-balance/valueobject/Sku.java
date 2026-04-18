package com.arka.inventory.domain.inventorybalance.valueobject;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;

public record Sku(String value) {

    public Sku {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("sku is required");
        }
        value = value.trim().toUpperCase();
    }

    public static Sku of(String value) {
        return new Sku(value);
    }
}
