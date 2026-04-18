package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertOrganizationUserProfileRequest(
        @Size(max = 100) String userProfileId,
        @NotBlank @Size(max = 100) String iamUserId,
        @Size(max = 255) String displayName,
        @Size(max = 120) String jobTitle,
        @Size(max = 120) String department,
        @Size(max = 20) String locale,
        @Size(max = 100) String timezone,
        @Size(max = 120) String roleReference,
        @Size(max = 50) String ownershipScope,
        @Size(max = 30) String status) {}
