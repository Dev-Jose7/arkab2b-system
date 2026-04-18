package com.arka.directory.infrastructure.adapter.out.persistence.mapper;

import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OutboxRowMapper {

    private final ObjectMapper eventPayloadObjectMapper;

    public OutboxRowMapper(ObjectMapper objectMapper) {
        this.eventPayloadObjectMapper = objectMapper
                .copy()
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public OutboxEventRow toRow(DomainEvent event) {
        Instant now = Instant.now();
        return new OutboxEventRow(
                event.eventId(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                toPayload(event),
                "PENDING",
                event.occurredAt(),
                null,
                0,
                null,
                now,
                now);
    }

    private String toPayload(DomainEvent event) {
        try {
            return eventPayloadObjectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize domain event payload", exception);
        }
    }
}
