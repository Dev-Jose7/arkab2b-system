package com.arka.reporting.application.command;

public record GenerateWeeklySalesReportCommand(
        String organizationId,
        String actorId,
        String weekId,
        String format,
        String idempotencyKey) {
}
