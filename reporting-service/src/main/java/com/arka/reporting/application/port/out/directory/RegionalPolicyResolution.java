package com.arka.reporting.application.port.out.directory;

public record RegionalPolicyResolution(
        String tenantId,
        String countryCode,
        boolean available,
        String policyRef) {
}
