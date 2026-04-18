package com.arka.reporting.application.command;

public record GenerateWeeklySalesReportCommand(
        String tenantId,
        String actorId,
        String weekId,
        String format,
        String idempotencyKey) {
}
