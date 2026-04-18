package com.arka.notification.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("channel_policies")
public record ChannelPolicyRow(
        @Id
        @Column("policy_id") String policyId,
        @Column("tenant_id") String tenantId,
        @Column("source_event_type") String sourceEventType,
        @Column("primary_channel") String primaryChannel,
        @Column("fallback_channel") String fallbackChannel,
        @Column("max_attempts") Integer maxAttempts,
        @Column("retry_interval_seconds") Integer retryIntervalSeconds,
        @Column("active") Boolean active,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}
