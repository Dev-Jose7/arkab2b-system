package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record AnalyticFactResponse(
        String factId,
        String tenantId,
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
