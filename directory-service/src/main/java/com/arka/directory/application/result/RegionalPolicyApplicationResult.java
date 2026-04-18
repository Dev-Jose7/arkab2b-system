package com.arka.directory.application.result;

public record RegionalPolicyApplicationResult(
        String organizationId,
        String countryCode,
        long policyVersion,
        String operationCode,
        String currencyCode,
        String timezone,
        String weeklyCutoffLocalTime,
        int reportingRetentionDays,
        String status) {}
