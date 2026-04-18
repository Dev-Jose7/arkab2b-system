package com.arka.reporting.domain.weeklyreportexecution.valueobject;

import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;

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
