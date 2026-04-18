package com.arka.reporting.application.result;

import java.time.Instant;

public record FactSearchItemResult(
        String factId,
        String sourceEventId,
        String eventType,
        String factType,
        String factStatus,
        String period,
        Instant occurredAt,
        Instant updatedAt) {
}
