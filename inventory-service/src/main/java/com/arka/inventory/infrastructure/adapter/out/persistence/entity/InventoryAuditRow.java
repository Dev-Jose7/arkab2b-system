package com.arka.inventory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("inventory_audits")
public record InventoryAuditRow(
        @Id @Column("audit_id") String auditId,
        @Column("tenant_id") String tenantId,
        @Column("actor_user_id") String actorUserId,
        @Column("action_type") String actionType,
        @Column("target_type") String targetType,
        @Column("target_id") String targetId,
        @Column("outcome") String outcome,
        @Column("payload") String payload,
        @Column("created_at") Instant createdAt) {}
