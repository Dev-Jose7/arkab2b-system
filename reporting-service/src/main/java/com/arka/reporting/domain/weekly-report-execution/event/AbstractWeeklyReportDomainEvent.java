package com.arka.reporting.domain.weeklyreportexecution.event;

import com.arka.reporting.domain.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractWeeklyReportDomainEvent implements DomainEvent {

    private final String eventId;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final Instant occurredAt;

    protected AbstractWeeklyReportDomainEvent(String eventType, String aggregateId, Instant occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateType = "WeeklyReportExecution";
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
