package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("idempotency_records")
public record IdempotencyRecordEntity(
        @Id String idempotencyId,
        String organizationId,
        String operationName,
        String idempotencyKey,
        String requestHash,
        String resourceType,
        String resourceId,
        int responseStatus,
        Instant createdAt,
        Instant updatedAt) {}
