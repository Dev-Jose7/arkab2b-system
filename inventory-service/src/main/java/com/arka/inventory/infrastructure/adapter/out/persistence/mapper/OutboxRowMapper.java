package com.arka.inventory.infrastructure.adapter.out.persistence.mapper;

import com.arka.inventory.domain.shared.event.DomainEvent;
import com.arka.inventory.domain.inventorybalance.event.CommitableAvailabilityRecalculated;
import com.arka.inventory.domain.inventorybalance.event.InventoryMutationEvent;
import com.arka.inventory.domain.inventorybalance.event.StockUpdated;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OutboxRowMapper {

    private final ObjectMapper objectMapper;

    public OutboxRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("eventId", event.eventId());
            payload.put("eventType", event.eventType());
            payload.put("occurredAt", event.occurredAt() == null ? null : event.occurredAt().toString());
            payload.put("aggregateId", event.aggregateId());
            payload.put("aggregateType", event.aggregateType());

            if (event instanceof StockUpdated stockUpdated) {
                payload.put("organizationId", stockUpdated.organizationId());
                payload.put("warehouseId", stockUpdated.warehouseId());
                payload.put("sku", stockUpdated.sku());
                payload.put("physicalQty", stockUpdated.physicalQty());
                payload.put("reservedQty", stockUpdated.reservedQty());
                payload.put("reason", stockUpdated.reason());
            } else if (event instanceof CommitableAvailabilityRecalculated availability) {
                payload.put("organizationId", availability.organizationId());
                payload.put("warehouseId", availability.warehouseId());
                payload.put("sku", availability.sku());
                payload.put("availableQty", availability.availableQty());
                payload.put("lowStock", availability.lowStock());
                payload.put("reason", availability.reason());
            } else if (event instanceof InventoryMutationEvent mutationEvent) {
                payload.put("organizationId", mutationEvent.organizationId());
                payload.put("mutationType", mutationEvent.mutationType());
                payload.put("targetType", mutationEvent.targetType());
                payload.put("targetId", mutationEvent.targetId());
                payload.put("actorUserId", mutationEvent.actorUserId());
            }

            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize domain event payload", exception);
        }
    }
}
