package com.arka.directory.application.result;

import java.time.Instant;

public record CountryPolicyResult(
        String policyId,
        String organizationId,
        String countryCode,
        long policyVersion,
        String currencyCode,
        String weekStartsOn,
        String weeklyCutoffLocalTime,
        String timezone,
        int reportingRetentionDays,
        boolean requiresVerifiedAddress,
        Instant effectiveFrom,
        Instant effectiveTo,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
