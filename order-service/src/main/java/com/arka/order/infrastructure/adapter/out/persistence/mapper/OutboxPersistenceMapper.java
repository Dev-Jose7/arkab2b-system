package com.arka.order.infrastructure.adapter.out.persistence.mapper;

import com.arka.order.application.port.out.persistence.PendingOutboxEvent;
import com.arka.order.domain.shared.event.DomainEvent;
import com.arka.order.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OutboxPersistenceMapper {

    private final ObjectMapper objectMapper;

    public OutboxPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEventEntity toEntity(DomainEvent event) {
        Instant now = Instant.now();
        return new OutboxEventEntity(
                event.eventId(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                writePayload(event),
                "PENDING",
                event.occurredAt(),
                null,
                0,
                null,
                now,
                now);
    }

    public PendingOutboxEvent toPending(OutboxEventEntity entity) {
        return new PendingOutboxEvent(
                entity.eventId(),
                entity.aggregateType(),
                entity.aggregateId(),
                entity.eventType(),
                entity.payload(),
                entity.retryCount());
    }

    private String writePayload(DomainEvent event) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", event.eventId());
            envelope.put("eventType", event.eventType());
            envelope.put("occurredAt", event.occurredAt());
            envelope.put("aggregateId", event.aggregateId());
            envelope.put("aggregateType", event.aggregateType());
            envelope.put("data", safeEventData(event));

            return objectMapper.writeValueAsString(envelope);
        } catch (Exception exception) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeEventData(DomainEvent event) {
        try {
            Map<String, Object> eventData = objectMapper.convertValue(event, Map.class);
            return eventData == null ? Map.of() : eventData;
        } catch (IllegalArgumentException exception) {
            return Map.of();
        }
    }
}
