package com.arka.notification.domain.notificationdispatch.entity;

import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationRequestStatus;
import com.arka.notification.domain.notificationdispatch.exception.DiscardedNotificationCannotDispatchException;
import com.arka.notification.domain.notificationdispatch.exception.InvalidNotificationStatusTransitionException;
import com.arka.notification.domain.notificationdispatch.exception.NotificationTerminalStateException;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public final class NotificationRequest {

    private final NotificationId notificationId;
    private final OrganizationId organizationId;
    private final String sourceEventId;
    private final String sourceEventType;
    private final String recipientRef;
    private final NotificationChannel channel;
    private final NotificationKey notificationKey;
    private final Instant createdAt;

    private String templateId;
    private String channelPolicyId;
    private String payloadJson;
    private NotificationRequestStatus status;
    private boolean retryable;
    private Instant nextRetryAt;
    private int maxAttempts;
    private int attemptCount;
    private String traceId;
    private String correlationId;
    private long version;
    private Instant updatedAt;

    private NotificationRequest(
            NotificationId notificationId,
            OrganizationId organizationId,
            String sourceEventId,
            String sourceEventType,
            String recipientRef,
            NotificationChannel channel,
            NotificationKey notificationKey,
            String templateId,
            String channelPolicyId,
            String payloadJson,
            NotificationRequestStatus status,
            boolean retryable,
            Instant nextRetryAt,
            int maxAttempts,
            int attemptCount,
            String traceId,
            String correlationId,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        this.notificationId = notificationId;
        this.organizationId = organizationId;
        this.sourceEventId = required(sourceEventId, "sourceEventId");
        this.sourceEventType = required(sourceEventType, "sourceEventType");
        this.recipientRef = required(recipientRef, "recipientRef");
        this.channel = channel;
        this.notificationKey = notificationKey;
        this.templateId = required(templateId, "templateId");
        this.channelPolicyId = required(channelPolicyId, "channelPolicyId");
        this.payloadJson = payloadJson == null ? "{}" : payloadJson.trim();
        this.status = status;
        this.retryable = retryable;
        this.nextRetryAt = nextRetryAt;
        this.maxAttempts = Math.max(maxAttempts, 1);
        this.attemptCount = Math.max(attemptCount, 0);
        this.traceId = traceId == null ? "" : traceId.trim();
        this.correlationId = correlationId == null ? "" : correlationId.trim();
        this.version = Math.max(version, 0L);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationRequest createPending(
            NotificationId notificationId,
            OrganizationId organizationId,
            String sourceEventId,
            String sourceEventType,
            String recipientRef,
            NotificationChannel channel,
            NotificationKey notificationKey,
            String templateId,
            String channelPolicyId,
            String payloadJson,
            int maxAttempts,
            Instant firstRetryAt,
            String traceId,
            String correlationId,
            Instant now) {
        return new NotificationRequest(
                notificationId,
                organizationId,
                sourceEventId,
                sourceEventType,
                recipientRef,
                channel,
                notificationKey,
                templateId,
                channelPolicyId,
                payloadJson,
                NotificationRequestStatus.PENDING,
                true,
                firstRetryAt,
                maxAttempts,
                0,
                traceId,
                correlationId,
                0L,
                now,
                now);
    }

    public static NotificationRequest rehydrate(
            NotificationId notificationId,
            OrganizationId organizationId,
            String sourceEventId,
            String sourceEventType,
            String recipientRef,
            NotificationChannel channel,
            NotificationKey notificationKey,
            String templateId,
            String channelPolicyId,
            String payloadJson,
            NotificationRequestStatus status,
            boolean retryable,
            Instant nextRetryAt,
            int maxAttempts,
            int attemptCount,
            String traceId,
            String correlationId,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        return new NotificationRequest(
                notificationId,
                organizationId,
                sourceEventId,
                sourceEventType,
                recipientRef,
                channel,
                notificationKey,
                templateId,
                channelPolicyId,
                payloadJson,
                status,
                retryable,
                nextRetryAt,
                maxAttempts,
                attemptCount,
                traceId,
                correlationId,
                version,
                createdAt,
                updatedAt);
    }

    public void registerAttempt(int attemptNumber, Instant now) {
        ensureNotDiscarded();
        ensureNotTerminalForDispatch();
        int expected = attemptCount + 1;
        if (attemptNumber != expected) {
            throw new DomainInvariantViolationException(
                    "secuencia_intento_invalida",
                    "attempt_number esperado=" + expected + " recibido=" + attemptNumber);
        }
        attemptCount = attemptNumber;
        updatedAt = now;
    }

    public void synchronizeAttemptCount(int persistedAttemptCount, Instant now) {
        int normalized = Math.max(persistedAttemptCount, 0);
        if (normalized == attemptCount) {
            return;
        }
        this.attemptCount = normalized;
        this.updatedAt = now;
    }

    public void markSent(Instant now) {
        transitionTo(NotificationRequestStatus.SENT, now);
        this.retryable = false;
        this.nextRetryAt = null;
    }

    public void markFailed(boolean canRetry, Instant nextRetryAt, Instant now) {
        if (status == NotificationRequestStatus.SENT || status == NotificationRequestStatus.DISCARDED) {
            throw new NotificationTerminalStateException(status.name());
        }
        transitionTo(NotificationRequestStatus.FAILED, now);
        this.retryable = canRetry;
        this.nextRetryAt = canRetry ? nextRetryAt : null;
    }

    public void discard(Instant now) {
        transitionTo(NotificationRequestStatus.DISCARDED, now);
        this.retryable = false;
        this.nextRetryAt = null;
    }

    public void scheduleRetry(Instant nextRetryAt, Instant now) {
        ensureNotDiscarded();
        if (status == NotificationRequestStatus.SENT) {
            throw new NotificationTerminalStateException(status.name());
        }
        this.retryable = true;
        this.nextRetryAt = nextRetryAt;
        this.updatedAt = now;
    }

    public boolean canDispatchAt(Instant now) {
        if (status == NotificationRequestStatus.DISCARDED || status == NotificationRequestStatus.SENT) {
            return false;
        }
        if (!retryable) {
            return false;
        }
        if (reachedMaxAttempts()) {
            return false;
        }
        return nextRetryAt == null || !now.isBefore(nextRetryAt);
    }

    public boolean reachedMaxAttempts() {
        return attemptCount >= maxAttempts;
    }

    public void bumpVersion() {
        this.version = this.version + 1;
    }

    private void transitionTo(NotificationRequestStatus targetStatus, Instant now) {
        if (status == targetStatus) {
            updatedAt = now;
            return;
        }
        if (status == NotificationRequestStatus.SENT || status == NotificationRequestStatus.DISCARDED) {
            throw new NotificationTerminalStateException(status.name());
        }
        if (status == NotificationRequestStatus.PENDING
                && targetStatus != NotificationRequestStatus.SENT
                && targetStatus != NotificationRequestStatus.FAILED
                && targetStatus != NotificationRequestStatus.DISCARDED) {
            throw new InvalidNotificationStatusTransitionException(status.name(), targetStatus.name());
        }
        if (status == NotificationRequestStatus.FAILED
                && targetStatus != NotificationRequestStatus.SENT
                && targetStatus != NotificationRequestStatus.FAILED
                && targetStatus != NotificationRequestStatus.DISCARDED) {
            throw new InvalidNotificationStatusTransitionException(status.name(), targetStatus.name());
        }
        this.status = targetStatus;
        this.updatedAt = now;
    }

    private void ensureNotDiscarded() {
        if (status == NotificationRequestStatus.DISCARDED) {
            throw new DiscardedNotificationCannotDispatchException();
        }
    }

    private void ensureNotTerminalForDispatch() {
        if (status == NotificationRequestStatus.SENT || status == NotificationRequestStatus.DISCARDED) {
            throw new NotificationTerminalStateException(status.name());
        }
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("solicitud_notificacion_invalida", field + " es obligatorio");
        }
        return value.trim();
    }

    public NotificationId notificationId() {
        return notificationId;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String sourceEventId() {
        return sourceEventId;
    }

    public String sourceEventType() {
        return sourceEventType;
    }

    public String recipientRef() {
        return recipientRef;
    }

    public NotificationChannel channel() {
        return channel;
    }

    public NotificationKey notificationKey() {
        return notificationKey;
    }

    public String templateId() {
        return templateId;
    }

    public String channelPolicyId() {
        return channelPolicyId;
    }

    public String payloadJson() {
        return payloadJson;
    }

    public NotificationRequestStatus status() {
        return status;
    }

    public boolean retryable() {
        return retryable;
    }

    public Instant nextRetryAt() {
        return nextRetryAt;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public int attemptCount() {
        return attemptCount;
    }

    public String traceId() {
        return traceId;
    }

    public String correlationId() {
        return correlationId;
    }

    public long version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
