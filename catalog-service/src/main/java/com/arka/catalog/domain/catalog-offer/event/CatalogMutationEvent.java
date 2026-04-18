package com.arka.catalog.domain.catalogoffer.event;

import java.time.Instant;

public final class CatalogMutationEvent extends AbstractCatalogDomainEvent {

    private final String type;

    public CatalogMutationEvent(String aggregateType, String aggregateId, String type, Instant occurredAt) {
        super(aggregateId, aggregateType, occurredAt);
        this.type = type;
    }

    @Override
    public String eventType() {
        return type;
    }
}
