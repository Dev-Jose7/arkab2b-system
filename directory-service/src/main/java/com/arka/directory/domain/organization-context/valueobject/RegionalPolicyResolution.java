package com.arka.directory.domain.organizationcontext.valueobject;

public record RegionalPolicyResolution(
        String organizationId,
        String countryCode,
        long policyVersion,
        String currencyCode,
        String timezone,
        String weeklyCutoffLocalTime,
        int reportingRetentionDays,
        boolean requiresVerifiedAddress,
        String operationCode,
        String actorUserId) {}
