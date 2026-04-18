package com.arka.notification.application.mapper.result;

import com.arka.notification.application.port.out.audit.NotificationAuditEntry;
import com.arka.notification.application.port.out.persistence.NotificationMetricsProjection;
import com.arka.notification.application.port.out.persistence.NotificationSearchProjection;
import com.arka.notification.application.port.out.persistence.ProviderCallbackProjection;
import com.arka.notification.application.result.NotificationAttemptResult;
import com.arka.notification.application.result.NotificationAuditEntryResult;
import com.arka.notification.application.result.NotificationMetricsResult;
import com.arka.notification.application.result.NotificationResult;
import com.arka.notification.application.result.NotificationSearchItemResult;
import com.arka.notification.application.result.ProviderCallbackResult;
import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NotificationResultMapper {

    public NotificationResult toNotificationResult(NotificationRequest request) {
        return new NotificationResult(
                request.notificationId().value(),
                request.organizationId().value(),
                request.sourceEventId(),
                request.sourceEventType(),
                request.recipientRef(),
                request.channel().name(),
                request.templateId(),
                request.channelPolicyId(),
                request.status().name(),
                request.retryable(),
                request.nextRetryAt(),
                request.maxAttempts(),
                request.attemptCount(),
                request.notificationKey().value(),
                request.traceId(),
                request.correlationId(),
                request.version(),
                request.createdAt(),
                request.updatedAt());
    }

    public NotificationAttemptResult toAttemptResult(NotificationAttempt attempt) {
        return new NotificationAttemptResult(
                attempt.attemptId().value(),
                attempt.notificationId().value(),
                attempt.attemptNumber(),
                attempt.resultStatus().name(),
                attempt.providerCode(),
                attempt.providerRef(),
                attempt.errorCode(),
                attempt.errorMessage(),
                attempt.retryable(),
                attempt.latencyMs(),
                attempt.createdAt());
    }

    public ProviderCallbackResult toCallbackResult(ProviderCallbackProjection projection) {
        return new ProviderCallbackResult(
                projection.callbackId(),
                projection.notificationId(),
                projection.providerCode(),
                projection.providerRef(),
                projection.callbackEventId(),
                projection.callbackStatus(),
                projection.receivedAt(),
                projection.updatedAt());
    }

    public NotificationSearchItemResult toSearchItemResult(NotificationSearchProjection projection) {
        return new NotificationSearchItemResult(
                projection.notificationId(),
                projection.sourceEventType(),
                projection.recipientRef(),
                projection.channel(),
                projection.status(),
                projection.attemptCount() == null ? 0 : projection.attemptCount(),
                projection.updatedAt());
    }

    public NotificationMetricsResult toMetricsResult(NotificationMetricsProjection projection) {
        if (projection == null) {
            return new NotificationMetricsResult(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return new NotificationMetricsResult(
                projection.pendingDispatchCount(),
                normalize(projection.deliverySuccessRate()),
                normalize(projection.discardRate()),
                normalize(projection.meanAttemptsToSuccess()),
                normalize(projection.providerTimeoutRate()));
    }

    public NotificationAuditEntryResult toAuditEntryResult(NotificationAuditEntry entry) {
        return new NotificationAuditEntryResult(
                entry.auditId(),
                entry.organizationId(),
                entry.actorId(),
                entry.actionType(),
                entry.targetType(),
                entry.targetId(),
                entry.outcome(),
                entry.payload(),
                entry.idempotencyKey(),
                entry.createdAt());
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
