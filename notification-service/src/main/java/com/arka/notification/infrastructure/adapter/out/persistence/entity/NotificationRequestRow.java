package com.arka.notification.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("notification_requests")
public record NotificationRequestRow(
        @Id
        @Column("notification_id") String notificationId,
        @Column("organization_id") String organizationId,
        @Column("source_event_id") String sourceEventId,
        @Column("source_event_type") String sourceEventType,
        @Column("recipient_ref") String recipientRef,
        @Column("channel") String channel,
        @Column("template_id") String templateId,
        @Column("channel_policy_id") String channelPolicyId,
        @Column("notification_key") String notificationKey,
        @Column("payload_json") String payloadJson,
        @Column("status") String status,
        @Column("retryable") Boolean retryable,
        @Column("next_retry_at") Instant nextRetryAt,
        @Column("max_attempts") Integer maxAttempts,
        @Column("attempt_count") Integer attemptCount,
        @Column("trace_id") String traceId,
        @Column("correlation_id") String correlationId,
        @Version
        @Column("version") Long version,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}
