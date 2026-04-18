package com.arka.catalog.domain.catalogoffer.valueobject;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;

public record TenantId(String value) {

    public TenantId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("tenant_requerido", "tenantId es obligatorio");
        }
        value = value.trim();
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }
}
