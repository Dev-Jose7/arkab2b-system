package com.arka.notification.domain.notificationdispatch.entity;

import java.time.Instant;

public record ProcessedEvent(
        String processedEventId,
        String eventId,
        String consumerName,
        Instant processedAt) {
}
