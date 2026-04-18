package com.arka.notification.infrastructure.adapter.out.persistence.mapper;

import com.arka.notification.application.port.out.audit.NotificationAuditEntry;
import com.arka.notification.application.port.out.persistence.ProviderCallbackProjection;
import com.arka.notification.domain.notificationdispatch.entity.ChannelPolicy;
import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.entity.NotificationTemplate;
import com.arka.notification.domain.notificationdispatch.entity.ProviderCallback;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationAttemptResultStatus;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationRequestStatus;
import com.arka.notification.domain.notificationdispatch.enumtype.ProviderCallbackStatus;
import com.arka.notification.domain.notificationdispatch.valueobject.AttemptId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import com.arka.notification.infrastructure.adapter.out.persistence.entity.ChannelPolicyRow;
import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationAttemptRow;
import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationAuditRow;
import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationRequestRow;
import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationTemplateRow;
import com.arka.notification.infrastructure.adapter.out.persistence.entity.ProviderCallbackRow;
import org.springframework.stereotype.Component;

@Component
public class NotificationRowMapper {

    public NotificationRequestRow toRow(NotificationRequest request) {
        return new NotificationRequestRow(
                request.notificationId().value(),
                request.organizationId().value(),
                request.sourceEventId(),
                request.sourceEventType(),
                request.recipientRef(),
                request.channel().name(),
                request.templateId(),
                request.channelPolicyId(),
                request.notificationKey().value(),
                request.payloadJson(),
                request.status().name(),
                request.retryable(),
                request.nextRetryAt(),
                request.maxAttempts(),
                request.attemptCount(),
                request.traceId(),
                request.correlationId(),
                request.version(),
                request.createdAt(),
                request.updatedAt());
    }

    public NotificationRequest toDomain(NotificationRequestRow row) {
        return NotificationRequest.rehydrate(
                NotificationId.of(row.notificationId()),
                OrganizationId.of(row.organizationId()),
                row.sourceEventId(),
                row.sourceEventType(),
                row.recipientRef(),
                NotificationChannel.valueOf(row.channel()),
                NotificationKey.of(row.notificationKey()),
                row.templateId(),
                row.channelPolicyId(),
                row.payloadJson(),
                NotificationRequestStatus.valueOf(row.status()),
                Boolean.TRUE.equals(row.retryable()),
                row.nextRetryAt(),
                row.maxAttempts() == null ? 1 : row.maxAttempts(),
                row.attemptCount() == null ? 0 : row.attemptCount(),
                row.traceId(),
                row.correlationId(),
                row.version() == null ? 0L : row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public NotificationAttemptRow toRow(NotificationAttempt attempt, String organizationId) {
        return new NotificationAttemptRow(
                attempt.attemptId().value(),
                organizationId,
                attempt.notificationId().value(),
                attempt.attemptNumber(),
                attempt.resultStatus().name(),
                attempt.providerCode(),
                emptyAsNull(attempt.providerRef()),
                emptyAsNull(attempt.errorCode()),
                emptyAsNull(attempt.errorMessage()),
                attempt.retryable(),
                attempt.latencyMs(),
                emptyAsNull(attempt.requestSnapshot()),
                emptyAsNull(attempt.responseSnapshot()),
                attempt.createdAt());
    }

    public NotificationAttempt toDomain(NotificationAttemptRow row) {
        return NotificationAttempt.rehydrate(
                AttemptId.of(row.attemptId()),
                NotificationId.of(row.notificationId()),
                row.attemptNumber() == null ? 1 : row.attemptNumber(),
                NotificationAttemptResultStatus.valueOf(row.resultStatus()),
                row.providerCode(),
                row.providerRef(),
                row.errorCode(),
                row.errorMessage(),
                Boolean.TRUE.equals(row.retryable()),
                row.latencyMs(),
                row.requestSnapshot(),
                row.responseSnapshot(),
                row.createdAt());
    }

    public NotificationTemplate toDomain(NotificationTemplateRow row) {
        return new NotificationTemplate(
                row.templateId(),
                row.organizationId(),
                row.sourceEventType(),
                NotificationChannel.valueOf(row.channel()),
                row.subjectTemplate(),
                row.bodyTemplate(),
                Boolean.TRUE.equals(row.active()),
                row.templateVersion());
    }

    public ChannelPolicy toDomain(ChannelPolicyRow row) {
        NotificationChannel fallback = row.fallbackChannel() == null || row.fallbackChannel().isBlank()
                ? null
                : NotificationChannel.valueOf(row.fallbackChannel());
        return new ChannelPolicy(
                row.policyId(),
                row.organizationId(),
                row.sourceEventType(),
                NotificationChannel.valueOf(row.primaryChannel()),
                fallback,
                row.maxAttempts() == null ? 1 : row.maxAttempts(),
                row.retryIntervalSeconds() == null ? 0 : row.retryIntervalSeconds(),
                Boolean.TRUE.equals(row.active()));
    }

    public ProviderCallbackRow toRow(ProviderCallback callback) {
        return new ProviderCallbackRow(
                callback.callbackId(),
                callback.organizationId(),
                callback.notificationId(),
                callback.providerCode(),
                callback.providerRef(),
                callback.callbackEventId(),
                callback.callbackStatus().name(),
                callback.payload(),
                callback.receivedAt(),
                callback.updatedAt());
    }

    public ProviderCallback toDomain(ProviderCallbackRow row) {
        return new ProviderCallback(
                row.callbackId(),
                row.organizationId(),
                row.notificationId(),
                row.providerCode(),
                row.providerRef(),
                row.callbackEventId(),
                ProviderCallbackStatus.valueOf(row.callbackStatus()),
                row.payload(),
                row.receivedAt(),
                row.updatedAt());
    }

    public ProviderCallbackProjection toProjection(ProviderCallbackRow row) {
        return new ProviderCallbackProjection(
                row.callbackId(),
                row.notificationId(),
                row.providerCode(),
                row.providerRef(),
                row.callbackEventId(),
                row.callbackStatus(),
                row.payload(),
                row.receivedAt(),
                row.updatedAt());
    }

    public NotificationAuditRow toRow(NotificationAuditEntry entry) {
        return new NotificationAuditRow(
                entry.auditId(),
                entry.organizationId(),
                entry.actorId(),
                entry.actionType(),
                entry.targetType(),
                entry.targetId(),
                entry.outcome(),
                entry.payload(),
                entry.idempotencyKey(),
                entry.payloadHash(),
                entry.createdAt());
    }

    public NotificationAuditEntry toDomain(NotificationAuditRow row) {
        return new NotificationAuditEntry(
                row.auditId(),
                row.organizationId(),
                row.actorId(),
                row.actionType(),
                row.targetType(),
                row.targetId(),
                row.outcome(),
                row.payload(),
                row.idempotencyKey(),
                row.payloadHash(),
                row.createdAt());
    }

    private String emptyAsNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
