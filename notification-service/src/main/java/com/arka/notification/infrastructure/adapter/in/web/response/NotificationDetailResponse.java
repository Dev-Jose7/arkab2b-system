package com.arka.notification.infrastructure.adapter.in.web.response;

import java.util.List;

public record NotificationDetailResponse(
        NotificationResponse notification,
        List<NotificationAttemptResponse> attempts,
        List<ProviderCallbackResponse> callbacks) {
}
