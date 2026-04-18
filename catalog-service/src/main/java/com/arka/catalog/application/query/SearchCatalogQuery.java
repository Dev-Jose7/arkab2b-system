package com.arka.catalog.application.query;

import java.time.Instant;

public record SearchCatalogQuery(
        String organizationId,
        String text,
        String brandId,
        String categoryId,
        String variantStatus,
        int page,
        int size,
        Instant at) {
}
