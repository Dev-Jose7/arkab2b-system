package com.arka.catalog.application.query;

public record GetCatalogAuditQuery(
        String organizationId,
        String targetType,
        String targetId,
        int page,
        int size) {
}
