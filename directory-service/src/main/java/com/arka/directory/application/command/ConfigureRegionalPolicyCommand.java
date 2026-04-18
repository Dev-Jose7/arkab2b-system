package com.arka.directory.application.command;

import java.time.Instant;

public record ConfigureRegionalPolicyCommand(
        String organizationId,
        String countryCode,
        String currencyCode,
        String weekStartsOn,
        String weeklyCutoffLocalTime,
        String timezone,
        Integer reportingRetentionDays,
        Boolean requiresVerifiedAddress,
        Instant effectiveFrom,
        String actorUserId,
        String actorOrganizationId) {}
