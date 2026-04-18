package com.arka.notification.domain.notificationdispatch.valueobject;

import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;

public record OrganizationId(String value) {

    public OrganizationId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("organization_invalida", "organizationId es obligatorio");
        }
        value = value.trim();
    }

    public static OrganizationId of(String value) {
        return new OrganizationId(value);
    }
}
