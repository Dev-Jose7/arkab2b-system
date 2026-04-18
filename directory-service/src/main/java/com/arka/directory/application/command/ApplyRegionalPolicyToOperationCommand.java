package com.arka.directory.application.command;

public record ApplyRegionalPolicyToOperationCommand(
        String organizationId,
        String countryCode,
        String operationCode,
        String actorUserId,
        String actorOrganizationId) {}
