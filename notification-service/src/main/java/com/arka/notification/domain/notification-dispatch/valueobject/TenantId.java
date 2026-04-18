package com.arka.notification.domain.notificationdispatch.valueobject;

import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;

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
