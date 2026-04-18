package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("carts")
public record CartEntity(
        @Id String cartId,
        String tenantId,
        String organizationId,
        String userId,
        String status,
        long version,
        Instant createdAt,
        Instant updatedAt) {}
