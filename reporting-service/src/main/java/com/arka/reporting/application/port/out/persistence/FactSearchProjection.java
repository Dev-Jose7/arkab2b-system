package com.arka.reporting.application.port.out.persistence;

import java.time.Instant;

public record FactSearchProjection(
        String factId,
        String sourceEventId,
        String eventType,
        String factType,
        String factStatus,
        String period,
        Instant occurredAt,
        Instant updatedAt) {
}
