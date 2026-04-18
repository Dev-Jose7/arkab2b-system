package com.arka.catalog.domain.shared.event;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    String eventType();

    Instant occurredAt();

    String aggregateId();

    String aggregateType();
}
