package com.arka.reporting.domain.shared.event;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    String eventType();

    String aggregateType();

    String aggregateId();

    Instant occurredAt();
}
