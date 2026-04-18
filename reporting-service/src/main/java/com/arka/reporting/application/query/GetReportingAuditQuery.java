package com.arka.reporting.application.query;

public record GetReportingAuditQuery(
        String organizationId,
        String targetType,
        String targetId,
        int page,
        int size) {
}
