package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;

public record OrganizationId(String value) {

    public OrganizationId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("organization_requerida", "organizationId es obligatorio");
        }
        value = value.trim();
    }

    public static OrganizationId of(String value) {
        return new OrganizationId(value);
    }
}
