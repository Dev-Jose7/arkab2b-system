package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("analytic_facts")
public record AnalyticFactRow(
        @Id
        @Column("fact_id") String factId,
        @Column("organization_id") String organizationId,
        @Column("source_event_id") String sourceEventId,
        @Column("event_type") String eventType,
        @Column("fact_type") String factType,
        @Column("raw_payload") String rawPayload,
        @Column("normalized_payload") String normalizedPayload,
        @Column("fact_status") String factStatus,
        @Column("rejection_reason") String rejectionReason,
        @Column("period") String period,
        @Column("occurred_at") Instant occurredAt,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}
