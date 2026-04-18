package com.arka.reporting.domain.shared.event;

import java.time.Instant;
import java.util.UUID;

public final class ReportingMutationEvent implements DomainEvent {

    private final String eventId;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final Instant occurredAt;

    public ReportingMutationEvent(
            String eventType,
            String aggregateType,
            String aggregateId,
            Instant occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public String aggregateType() {
        return aggregateType;
    }

    @Override
    public String aggregateId() {
        return aggregateId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
