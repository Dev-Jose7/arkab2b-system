package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_status_histories")
public record OrderStatusHistoryEntity(
        @Id String statusHistoryId,
        String orderId,
        String organizationId,
        String actorUserId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant occurredAt) {}
