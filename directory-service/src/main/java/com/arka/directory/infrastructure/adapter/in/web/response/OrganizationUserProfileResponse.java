package com.arka.directory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record OrganizationUserProfileResponse(
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
