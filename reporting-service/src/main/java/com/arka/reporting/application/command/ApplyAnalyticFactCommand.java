package com.arka.reporting.application.command;

public record ApplyAnalyticFactCommand(
        String organizationId,
        String actorId,
        String factId,
        String idempotencyKey) {
}
