package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.time.Instant;
import java.util.List;

public record VariantResponse(
        String variantId,
        String tenantId,
        String productId,
        String sku,
        String name,
        String description,
        String status,
        Instant sellableFrom,
        Instant sellableUntil,
        Integer weightGrams,
        List<VariantAttributeResponse> attributes,
        Instant createdAt,
        Instant updatedAt) {
}
