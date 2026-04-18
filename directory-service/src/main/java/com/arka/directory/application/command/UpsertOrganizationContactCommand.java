package com.arka.directory.application.command;

public record UpsertOrganizationContactCommand(
        String organizationId,
        String contactId,
        String contactType,
        String label,
        String value,
        String valueMasked,
        Boolean primary,
        String status,
        String actorUserId,
        String actorOrganizationId) {}
