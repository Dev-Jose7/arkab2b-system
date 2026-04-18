package com.arka.directory.domain.countrypolicy.event;

import java.time.Instant;

public final class RegionalPolicyConfigured extends AbstractDirectoryDomainEvent {

    private final String countryCode;
    private final long policyVersion;

    public RegionalPolicyConfigured(
            Instant occurredAt,
            String organizationId,
            String countryCode,
            long policyVersion) {
        super("RegionalPolicyConfigured", occurredAt, organizationId, "CountryPolicy");
        this.countryCode = countryCode;
        this.policyVersion = policyVersion;
    }

    public String countryCode() {
        return countryCode;
    }

    public long policyVersion() {
        return policyVersion;
    }
}
