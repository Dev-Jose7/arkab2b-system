package com.arka.reporting.application.query;

public record GetReportingAuditQuery(
        String tenantId,
        String targetType,
        String targetId,
        int page,
        int size) {
}
