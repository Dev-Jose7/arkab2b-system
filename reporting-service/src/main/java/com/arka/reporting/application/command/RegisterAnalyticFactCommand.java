package com.arka.reporting.application.command;

import java.time.Instant;

public record RegisterAnalyticFactCommand(
        String organizationId,
        String actorId,
        String sourceEventId,
        String sourceEventType,
        String factType,
        String payloadJson,
        Instant occurredAt,
        String consumerName,
        String idempotencyKey) {
}
