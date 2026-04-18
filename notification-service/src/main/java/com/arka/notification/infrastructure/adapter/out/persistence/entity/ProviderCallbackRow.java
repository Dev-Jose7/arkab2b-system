package com.arka.notification.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("provider_callbacks")
public record ProviderCallbackRow(
        @Id
        @Column("callback_id") String callbackId,
        @Column("organization_id") String organizationId,
        @Column("notification_id") String notificationId,
        @Column("provider_code") String providerCode,
        @Column("provider_ref") String providerRef,
        @Column("callback_event_id") String callbackEventId,
        @Column("callback_status") String callbackStatus,
        @Column("payload") String payload,
        @Column("received_at") Instant receivedAt,
        @Column("updated_at") Instant updatedAt) {
}
