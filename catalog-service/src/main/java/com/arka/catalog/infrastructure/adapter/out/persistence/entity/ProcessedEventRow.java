package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("processed_events")
public record ProcessedEventRow(
        @Id
        @Column("processed_event_id") String processedEventId,
        @Column("event_id") String eventId,
        @Column("consumer_name") String consumerName,
        @Column("processed_at") Instant processedAt) {
}
