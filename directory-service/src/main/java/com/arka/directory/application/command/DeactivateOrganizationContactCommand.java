package com.arka.directory.application.command;

public record DeactivateOrganizationContactCommand(
        String organizationId,
        String contactId,
        String actorUserId,
        String actorOrganizationId) {}
