package com.arka.reporting.domain.weeklyreportexecution.valueobject;

import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;

public record TenantId(String value) {

    public TenantId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("tenant_invalido", "tenantId es obligatorio");
        }
        value = value.trim();
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }
}
