package com.arka.catalog.domain.catalogoffer.event;

import com.arka.catalog.domain.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public abstract class AbstractCatalogDomainEvent implements DomainEvent {

    private final String eventId;
    private final Instant occurredAt;
    private final String aggregateId;
    private final String aggregateType;

    protected AbstractCatalogDomainEvent(String aggregateId, String aggregateType, Instant occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventId() {
        return eventId;
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
