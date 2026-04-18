package com.arka.inventory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("idempotency_records")
public record IdempotencyRecordRow(
        @Id @Column("idempotency_id") String idempotencyId,
        @Column("organization_id") String organizationId,
        @Column("operation_name") String operationName,
        @Column("idempotency_key") String idempotencyKey,
        @Column("request_hash") String requestHash,
        @Column("resource_type") String resourceType,
        @Column("resource_id") String resourceId,
        @Column("response_status") Integer responseStatus,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
