package com.arka.notification.application.result;

import java.util.List;

public record NotificationDetailResult(
        NotificationResult notification,
        List<NotificationAttemptResult> attempts,
        List<ProviderCallbackResult> callbacks) {
}
