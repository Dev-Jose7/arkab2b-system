package com.arka.reporting.domain.weeklyreportexecution.entity;

import java.time.Instant;

public record ProcessedEvent(
        String processedEventId,
        String eventId,
        String consumerName,
        Instant processedAt) {
}
