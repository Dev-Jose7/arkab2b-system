package com.arka.directory.domain.countrypolicy.event;

import java.time.Instant;

public final class RegionalPolicyAppliedInOperation extends AbstractDirectoryDomainEvent {

    private final String countryCode;
    private final long policyVersion;
    private final String operationCode;
    private final String actorUserId;

    public RegionalPolicyAppliedInOperation(
            Instant occurredAt,
            String organizationId,
            String countryCode,
            long policyVersion,
            String operationCode,
            String actorUserId) {
        super("RegionalPolicyAppliedInOperation", occurredAt, organizationId, "CountryPolicy");
        this.countryCode = countryCode;
        this.policyVersion = policyVersion;
        this.operationCode = operationCode;
        this.actorUserId = actorUserId;
    }

    public String countryCode() {
        return countryCode;
    }

    public long policyVersion() {
        return policyVersion;
    }

    public String operationCode() {
        return operationCode;
    }

    public String actorUserId() {
        return actorUserId;
    }
}
