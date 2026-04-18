package com.arka.catalog.application.port.out.directory;

public record RegionalPolicyContext(
        String policyReference,
        String countryCode,
        String currencyCode) {
}
