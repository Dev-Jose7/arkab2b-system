package com.arka.directory.domain.organizationcontext.event;

import com.arka.directory.domain.countrypolicy.event.AbstractDirectoryDomainEvent;
import java.time.Instant;

public final class OrganizationRegistered extends AbstractDirectoryDomainEvent {

    private final String organizationCode;
    private final String countryCode;

    public OrganizationRegistered(
            Instant occurredAt,
            String organizationId,
            String organizationCode,
            String countryCode) {
        super("OrganizationRegistered", occurredAt, organizationId, "Organization");
        this.organizationCode = organizationCode;
        this.countryCode = countryCode;
    }

    public String organizationCode() {
        return organizationCode;
    }

    public String countryCode() {
        return countryCode;
    }
}
