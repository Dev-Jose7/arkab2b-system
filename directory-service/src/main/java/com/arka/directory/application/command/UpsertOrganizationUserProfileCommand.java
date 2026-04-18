package com.arka.directory.application.command;

public record UpsertOrganizationUserProfileCommand(
        String organizationId,
        String userProfileId,
        String iamUserId,
        String displayName,
        String jobTitle,
        String department,
        String locale,
        String timezone,
        String roleReference,
        String ownershipScope,
        String status,
        String actorUserId,
        String actorOrganizationId) {}
