package com.arka.notification.domain.notificationdispatch.event;

import com.arka.notification.domain.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractNotificationDomainEvent implements DomainEvent {

    private final String eventId;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final Instant occurredAt;

    protected AbstractNotificationDomainEvent(
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
