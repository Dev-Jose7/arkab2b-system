package com.arka.catalog.application.result;

import java.time.Instant;
import java.util.List;

public record VariantResult(
        String variantId,
        String organizationId,
        String productId,
        String sku,
        String name,
        String description,
        String status,
        Instant sellableFrom,
        Instant sellableUntil,
        Integer weightGrams,
        List<VariantAttributeResult> attributes,
        Instant createdAt,
        Instant updatedAt) {
}
