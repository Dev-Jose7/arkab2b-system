package com.arka.reporting.application.query;

public record GetWeeklyExecutionQuery(
        String tenantId,
        String executionId,
        String weekId,
        String reportType) {
}
