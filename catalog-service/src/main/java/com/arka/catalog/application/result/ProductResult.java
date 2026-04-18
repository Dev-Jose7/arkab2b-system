package com.arka.catalog.application.result;

import java.time.Instant;
import java.util.List;

public record ProductResult(
        String productId,
        String organizationId,
        String productCode,
        String name,
        String description,
        String brandId,
        String categoryId,
        String status,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt) {
}
