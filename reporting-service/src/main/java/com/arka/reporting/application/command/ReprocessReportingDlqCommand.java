package com.arka.reporting.application.command;

public record ReprocessReportingDlqCommand(
        String organizationId,
        String actorId,
        String dlqEventId,
        String consumerName,
        String factId,
        String idempotencyKey) {
}
