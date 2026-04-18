package com.arka.catalog.infrastructure.adapter.out.persistence.mapper;

import com.arka.catalog.domain.shared.event.DomainEvent;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OutboxRowMapper {

    public OutboxEventRow toRow(DomainEvent event, String payload) {
        Instant now = Instant.now();
        return new OutboxEventRow(
                event.eventId(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                payload,
                "PENDING",
                event.occurredAt(),
                null,
                0,
                null,
                now,
                now);
    }
}
