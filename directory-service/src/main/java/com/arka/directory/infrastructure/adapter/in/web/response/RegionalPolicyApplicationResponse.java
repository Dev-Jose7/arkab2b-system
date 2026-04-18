package com.arka.directory.infrastructure.adapter.in.web.response;

public record RegionalPolicyApplicationResponse(
        String organizationId,
        String countryCode,
        long policyVersion,
        String operationCode,
        String currencyCode,
        String timezone,
        String weeklyCutoffLocalTime,
        int reportingRetentionDays,
        String status) {}
