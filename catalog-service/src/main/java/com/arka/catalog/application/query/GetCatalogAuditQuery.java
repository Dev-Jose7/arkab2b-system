package com.arka.catalog.application.query;

public record GetCatalogAuditQuery(
        String tenantId,
        String targetType,
        String targetId,
        int page,
        int size) {
}
