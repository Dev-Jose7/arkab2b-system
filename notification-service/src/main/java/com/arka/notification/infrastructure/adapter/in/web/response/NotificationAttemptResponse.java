package com.arka.notification.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record NotificationAttemptResponse(
        String attemptId,
        String notificationId,
        int attemptNumber,
        String resultStatus,
        String providerCode,
        String providerRef,
        String errorCode,
        String errorMessage,
        boolean retryable,
        Long latencyMs,
        Instant createdAt) {
}
