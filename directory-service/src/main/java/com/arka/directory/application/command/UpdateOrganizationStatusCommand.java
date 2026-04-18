package com.arka.directory.application.command;

public record UpdateOrganizationStatusCommand(
        String organizationId,
        String status,
        String actorUserId,
        String actorOrganizationId) {}
