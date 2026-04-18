package com.arka.notification.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("notification_attempts")
public record NotificationAttemptRow(
        @Id
        @Column("attempt_id") String attemptId,
        @Column("organization_id") String organizationId,
        @Column("notification_id") String notificationId,
        @Column("attempt_number") Integer attemptNumber,
        @Column("result_status") String resultStatus,
        @Column("provider_code") String providerCode,
        @Column("provider_ref") String providerRef,
        @Column("error_code") String errorCode,
        @Column("error_message") String errorMessage,
        @Column("retryable") Boolean retryable,
        @Column("latency_ms") Long latencyMs,
        @Column("request_snapshot") String requestSnapshot,
        @Column("response_snapshot") String responseSnapshot,
        @Column("created_at") Instant createdAt) {
}
