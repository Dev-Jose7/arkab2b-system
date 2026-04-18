package com.arka.reporting.application.query;

public record GetWeeklyExecutionQuery(
        String organizationId,
        String executionId,
        String weekId,
        String reportType) {
}
