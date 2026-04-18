package com.arka.notification.application.result;

import java.time.Instant;

public record NotificationTimelineItemResult(
        String type,
        String reference,
        String status,
        String providerRef,
        Instant occurredAt,
        String payload) {
}
