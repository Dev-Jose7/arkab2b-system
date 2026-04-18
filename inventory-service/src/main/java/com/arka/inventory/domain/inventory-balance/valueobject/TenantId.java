package com.arka.inventory.domain.inventorybalance.valueobject;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;

public record TenantId(String value) {

    public TenantId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("tenantId is required");
        }
        value = value.trim();
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }
}
