package com.arka.reporting.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record RegisterAnalyticFactRequest(
        @NotBlank(message = "sourceEventId es obligatorio") String sourceEventId,
        @NotBlank(message = "sourceEventType es obligatorio") String sourceEventType,
        String factType,
        String payloadJson,
        Instant occurredAt,
        String consumerName,
        String idempotencyKey) {
}
