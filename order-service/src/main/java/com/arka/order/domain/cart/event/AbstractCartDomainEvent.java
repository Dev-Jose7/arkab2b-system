package com.arka.order.domain.cart.event;

import com.arka.order.domain.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractCartDomainEvent implements DomainEvent {

    private final String eventId;
    private final String eventType;
    private final Instant occurredAt;
    private final String aggregateId;
    private final String aggregateType;

    protected AbstractCartDomainEvent(
            String eventType,
            Instant occurredAt,
            String aggregateId,
            String aggregateType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
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
    public Instant occurredAt() {
        return occurredAt;
    }

    @Override
    public String aggregateId() {
        return aggregateId;
    }

    @Override
    public String aggregateType() {
        return aggregateType;
    }
}
