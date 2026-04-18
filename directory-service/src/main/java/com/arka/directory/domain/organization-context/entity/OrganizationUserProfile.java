package com.arka.directory.domain.organizationcontext.entity;

import com.arka.directory.domain.organizationcontext.enumtype.OrganizationUserProfileStatus;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record OrganizationUserProfile(
        String userProfileId,
        String organizationId,
        String iamUserId,
        String displayName,
        String jobTitle,
        String department,
        String locale,
        String timezone,
        String roleReference,
        String ownershipScope,
        OrganizationUserProfileStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public OrganizationUserProfile {
        requireNotBlank(userProfileId, "userProfileId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(iamUserId, "iamUserId");
        if (status == null) {
            throw new DomainInvariantViolationException("Organization user profile status is required");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public OrganizationUserProfile deactivateFromUserBlocked(Instant updatedAt) {
        return withStatus(OrganizationUserProfileStatus.INACTIVE, updatedAt);
    }

    public OrganizationUserProfile withStatus(OrganizationUserProfileStatus newStatus, Instant updatedAt) {
        if (newStatus == null) {
            throw new DomainInvariantViolationException("Organization user profile status is required");
        }
        return new OrganizationUserProfile(
                userProfileId,
                organizationId,
                iamUserId,
                displayName,
                jobTitle,
                department,
                locale,
                timezone,
                roleReference,
                ownershipScope,
                newStatus,
                createdAt,
                updatedAt == null ? Instant.now() : updatedAt);
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
