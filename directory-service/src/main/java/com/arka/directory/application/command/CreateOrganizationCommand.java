package com.arka.directory.application.command;

public record CreateOrganizationCommand(
        String organizationCode,
        String legalName,
        String tradeName,
        String countryCode,
        String currencyCode,
        String timezone,
        String segmentTier,
        String actorUserId,
        String actorOrganizationId) {}
