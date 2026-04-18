package com.arka.reporting.application.port.out.persistence;

public record FactSearchFilter(
        String tenantId,
        String eventType,
        String factType,
        String period,
        String status,
        int offset,
        int limit) {
}
