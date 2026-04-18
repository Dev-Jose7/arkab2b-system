package com.arka.notification.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record NotificationTimelineItemResponse(
        String type,
        String reference,
        String status,
        String providerRef,
        Instant occurredAt,
        String payload) {
}
