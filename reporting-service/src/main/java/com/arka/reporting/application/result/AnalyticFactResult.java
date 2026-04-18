package com.arka.reporting.application.result;

import java.time.Instant;

public record AnalyticFactResult(
        String factId,
        String organizationId,
        String sourceEventId,
        String eventType,
        String factType,
        String factStatus,
        String rawPayload,
        String normalizedPayload,
        String rejectionReason,
        Instant occurredAt,
        Instant createdAt,
        Instant updatedAt) {
}
