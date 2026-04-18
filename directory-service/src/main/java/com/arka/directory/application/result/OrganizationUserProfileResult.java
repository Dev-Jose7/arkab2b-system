package com.arka.directory.application.result;

import java.time.Instant;

public record OrganizationUserProfileResult(
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
        String status,
        Instant createdAt,
        Instant updatedAt) {}
