package com.arka.inventory.domain.inventorybalance.valueobject;

import com.arka.inventory.domain.shared.exception.DomainInvariantViolationException;

public record OrganizationId(String value) {

    public OrganizationId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("organizationId is required");
        }
        value = value.trim();
    }

    public static OrganizationId of(String value) {
        return new OrganizationId(value);
    }
}
