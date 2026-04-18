package com.arka.notification.application.result;

import java.time.Instant;

public record NotificationAttemptResult(
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
