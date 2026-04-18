package com.arka.directory.application.command;

public record HandleIamUserBlockedCommand(
        String organizationId,
        String iamUserId,
        String actorUserId,
        String actorOrganizationId) {}
