package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("reporting_audits")
public record ReportingAuditRow(
        @Id
        @Column("audit_id") String auditId,
        @Column("organization_id") String organizationId,
        @Column("actor_id") String actorId,
        @Column("action_type") String actionType,
        @Column("target_type") String targetType,
        @Column("target_id") String targetId,
        @Column("outcome") String outcome,
        @Column("payload") String payload,
        @Column("idempotency_key") String idempotencyKey,
        @Column("payload_hash") String payloadHash,
        @Column("created_at") Instant createdAt) {
}
