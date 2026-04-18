package com.arka.reporting.domain.analyticfact.event;

import java.time.Instant;

public final class AnalyticFactApplied extends AbstractAnalyticFactDomainEvent {

    private final String tenantId;
    private final String sourceEventId;
    private final String factType;

    public AnalyticFactApplied(String factId, String tenantId, String sourceEventId, String factType, Instant occurredAt) {
        super("AnalyticFactApplied", factId, occurredAt);
        this.tenantId = tenantId;
        this.sourceEventId = sourceEventId;
        this.factType = factType;
    }

    public String tenantId() {
        return tenantId;
    }

    public String sourceEventId() {
        return sourceEventId;
    }

    public String factType() {
        return factType;
    }
}
