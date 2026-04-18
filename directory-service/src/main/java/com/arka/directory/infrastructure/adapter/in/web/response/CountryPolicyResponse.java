package com.arka.directory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record CountryPolicyResponse(
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
