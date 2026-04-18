package com.arka.catalog.application.port.out.persistence;

import java.time.Instant;

public record CatalogSearchFilter(
        String tenantId,
        String text,
        String brandId,
        String categoryId,
        String variantStatus,
        int offset,
        int limit,
        Instant at) {
}
