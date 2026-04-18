package com.arka.reporting.domain.analyticfact.event;

import java.time.Instant;

public final class AnalyticFactApplied extends AbstractAnalyticFactDomainEvent {

    private final String organizationId;
    private final String sourceEventId;
    private final String factType;

    public AnalyticFactApplied(String factId, String organizationId, String sourceEventId, String factType, Instant occurredAt) {
        super("AnalyticFactApplied", factId, occurredAt);
        this.organizationId = organizationId;
        this.sourceEventId = sourceEventId;
        this.factType = factType;
    }

    public String organizationId() {
        return organizationId;
    }

    public String sourceEventId() {
        return sourceEventId;
    }

    public String factType() {
        return factType;
    }
}
