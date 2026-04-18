package com.arka.directory.domain.organizationcontext.entity;

import com.arka.directory.domain.organizationcontext.enumtype.ContactType;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationContactStatus;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record OrganizationContact(
        String contactId,
        String organizationId,
        ContactType contactType,
        String label,
        String valueNormalized,
        String valueMasked,
        boolean primary,
        OrganizationContactStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public OrganizationContact {
        requireNotBlank(contactId, "contactId");
        requireNotBlank(organizationId, "organizationId");
        if (contactType == null) {
            throw new DomainInvariantViolationException("contactType is required");
        }
        valueNormalized = normalize(valueNormalized);
        if (status == null) {
            throw new DomainInvariantViolationException("contact status is required");
        }
        if (primary && !status.isActive()) {
            throw new DomainInvariantViolationException("Primary contact must be active");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public OrganizationContact deactivate(Instant updatedAt) {
        return new OrganizationContact(
                contactId,
                organizationId,
                contactType,
                label,
                valueNormalized,
                valueMasked,
                false,
                OrganizationContactStatus.INACTIVE,
                createdAt,
                updatedAt);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("contact value is required");
        }
        return value.trim().toUpperCase();
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
