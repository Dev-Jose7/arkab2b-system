package com.arka.reporting.application.command;

public record GenerateWeeklyReplenishmentReportCommand(
        String organizationId,
        String actorId,
        String weekId,
        String format,
        String idempotencyKey) {
}
