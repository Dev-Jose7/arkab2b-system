package com.arka.directory.domain.organizationcontext.event;

import com.arka.directory.domain.countrypolicy.event.AbstractDirectoryDomainEvent;
import java.time.Instant;

public final class OrganizationStatusChanged extends AbstractDirectoryDomainEvent {

    private final String previousStatus;
    private final String currentStatus;

    public OrganizationStatusChanged(
            Instant occurredAt,
            String organizationId,
            String previousStatus,
            String currentStatus) {
        super("OrganizationStatusChanged", occurredAt, organizationId, "Organization");
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
    }

    public String previousStatus() {
        return previousStatus;
    }

    public String currentStatus() {
        return currentStatus;
    }
}
