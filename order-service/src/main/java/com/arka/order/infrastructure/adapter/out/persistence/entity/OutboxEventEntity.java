package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("outbox_events")
public record OutboxEventEntity(
        @Id String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        String status,
        Instant occurredAt,
        Instant publishedAt,
        int retryCount,
        String lastError,
        Instant createdAt,
        Instant updatedAt) {}
