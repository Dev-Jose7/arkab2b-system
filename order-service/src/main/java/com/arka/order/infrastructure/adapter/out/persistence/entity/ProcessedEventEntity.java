package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("processed_events")
public record ProcessedEventEntity(
        @Id String processedEventId,
        String eventId,
        String consumerName,
        Instant processedAt) {}
