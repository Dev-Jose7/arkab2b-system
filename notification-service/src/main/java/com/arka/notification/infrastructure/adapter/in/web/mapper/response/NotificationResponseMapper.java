package com.arka.notification.infrastructure.adapter.in.web.mapper.response;

import com.arka.notification.application.result.NotificationAttemptResult;
import com.arka.notification.application.result.NotificationAuditEntryResult;
import com.arka.notification.application.result.NotificationAuditResult;
import com.arka.notification.application.result.NotificationDetailResult;
import com.arka.notification.application.result.NotificationMetricsResult;
import com.arka.notification.application.result.NotificationResult;
import com.arka.notification.application.result.NotificationSearchItemResult;
import com.arka.notification.application.result.NotificationSearchResult;
import com.arka.notification.application.result.NotificationTimelineItemResult;
import com.arka.notification.application.result.NotificationTimelineResult;
import com.arka.notification.application.result.ProviderCallbackResult;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationAttemptResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationAuditEntryResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationAuditResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationDetailResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationMetricsResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationSearchItemResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationSearchResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationTimelineItemResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationTimelineResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.ProviderCallbackResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationResponseMapper {

    public NotificationResponse toResponse(NotificationResult result) {
        return new NotificationResponse(
                result.notificationId(),
                result.tenantId(),
                result.sourceEventId(),
                result.sourceEventType(),
                result.recipientRef(),
                result.channel(),
                result.templateId(),
                result.channelPolicyId(),
                result.status(),
                result.retryable(),
                result.nextRetryAt(),
                result.maxAttempts(),
                result.attemptCount(),
                result.notificationKey(),
                result.traceId(),
                result.correlationId(),
                result.version(),
                result.createdAt(),
                result.updatedAt());
    }

    public NotificationAttemptResponse toResponse(NotificationAttemptResult result) {
        return new NotificationAttemptResponse(
                result.attemptId(),
                result.notificationId(),
                result.attemptNumber(),
                result.resultStatus(),
                result.providerCode(),
                result.providerRef(),
                result.errorCode(),
                result.errorMessage(),
                result.retryable(),
                result.latencyMs(),
                result.createdAt());
    }

    public ProviderCallbackResponse toResponse(ProviderCallbackResult result) {
        return new ProviderCallbackResponse(
                result.callbackId(),
                result.notificationId(),
                result.providerCode(),
                result.providerRef(),
                result.callbackEventId(),
                result.callbackStatus(),
                result.receivedAt(),
                result.updatedAt());
    }

    public NotificationDetailResponse toResponse(NotificationDetailResult result) {
        return new NotificationDetailResponse(
                toResponse(result.notification()),
                result.attempts().stream().map(this::toResponse).toList(),
                result.callbacks().stream().map(this::toResponse).toList());
    }

    public NotificationSearchResponse toResponse(NotificationSearchResult result) {
        return new NotificationSearchResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements());
    }

    public NotificationSearchItemResponse toResponse(NotificationSearchItemResult result) {
        return new NotificationSearchItemResponse(
                result.notificationId(),
                result.sourceEventType(),
                result.recipientRef(),
                result.channel(),
                result.status(),
                result.attemptCount(),
                result.updatedAt());
    }

    public NotificationTimelineResponse toResponse(NotificationTimelineResult result) {
        return new NotificationTimelineResponse(
                result.notificationId(),
                result.items().stream().map(this::toResponse).toList());
    }

    public NotificationTimelineItemResponse toResponse(NotificationTimelineItemResult result) {
        return new NotificationTimelineItemResponse(
                result.type(),
                result.reference(),
                result.status(),
                result.providerRef(),
                result.occurredAt(),
                result.payload());
    }

    public NotificationMetricsResponse toResponse(NotificationMetricsResult result) {
        return new NotificationMetricsResponse(
                result.pendingDispatchCount(),
                result.deliverySuccessRate(),
                result.discardRate(),
                result.meanAttemptsToSuccess(),
                result.providerTimeoutRate());
    }

    public NotificationAuditResponse toResponse(NotificationAuditResult result) {
        return new NotificationAuditResponse(
                result.entries().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements());
    }

    public NotificationAuditEntryResponse toResponse(NotificationAuditEntryResult result) {
        return new NotificationAuditEntryResponse(
                result.auditId(),
                result.tenantId(),
                result.actorId(),
                result.actionType(),
                result.targetType(),
                result.targetId(),
                result.outcome(),
                result.payload(),
                result.idempotencyKey(),
                result.createdAt());
    }
}
