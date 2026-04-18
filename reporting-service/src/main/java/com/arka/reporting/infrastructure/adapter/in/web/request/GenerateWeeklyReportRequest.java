package com.arka.reporting.infrastructure.adapter.in.web.request;

public record GenerateWeeklyReportRequest(
        String weekId,
        String format,
        String idempotencyKey) {
}
