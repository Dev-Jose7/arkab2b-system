package com.arka.directory.application.command;

public record DeactivateOrganizationUserProfileCommand(
        String organizationId,
        String iamUserId,
        String reason,
        String actorUserId,
        String actorOrganizationId) {}
