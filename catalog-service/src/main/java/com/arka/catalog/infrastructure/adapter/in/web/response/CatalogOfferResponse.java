package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record CatalogOfferResponse(
        String offerId,
        String tenantId,
        String productId,
        String variantId,
        String priceId,
        String regionalPolicyReference,
        Instant publishedAt,
        Instant updatedAt) {
}
