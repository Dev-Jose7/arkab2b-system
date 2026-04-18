package com.arka.directory.domain.organizationcontext.valueobject;

import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;

public record OrganizationContext(
        OrganizationId organizationId,
        String actorUserId,
        String actorOrganizationId,
        boolean actorLegitimate,
        boolean directoryAdmin) {

    public OrganizationContext {
        if (organizationId == null) {
            throw new DomainInvariantViolationException("organizationId is required");
        }
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new DomainInvariantViolationException("actorUserId is required");
        }
        actorUserId = actorUserId.trim();
        actorOrganizationId = actorOrganizationId == null ? "" : actorOrganizationId.trim();
    }

    public boolean hasScopedOrganization() {
        return !actorOrganizationId.isBlank();
    }
}
