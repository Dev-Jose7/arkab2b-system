package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record FactSearchItemResponse(
        String factId,
        String sourceEventId,
        String eventType,
        String factType,
        String factStatus,
        String period,
        Instant occurredAt,
        Instant updatedAt) {
}
