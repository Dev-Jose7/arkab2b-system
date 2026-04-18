package com.arka.reporting.application.port.out.directory;

public record RegionalPolicyResolution(
        String organizationId,
        String countryCode,
        boolean available,
        String policyRef) {
}
