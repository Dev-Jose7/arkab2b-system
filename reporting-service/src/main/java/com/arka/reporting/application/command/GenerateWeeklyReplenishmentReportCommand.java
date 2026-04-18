package com.arka.reporting.application.command;

public record GenerateWeeklyReplenishmentReportCommand(
        String tenantId,
        String actorId,
        String weekId,
        String format,
        String idempotencyKey) {
}
