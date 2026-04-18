package com.arka.reporting.application.command;

public record ApplyAnalyticFactCommand(
        String tenantId,
        String actorId,
        String factId,
        String idempotencyKey) {
}
