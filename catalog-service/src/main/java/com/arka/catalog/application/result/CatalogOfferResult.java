package com.arka.catalog.application.result;

import java.time.Instant;

public record CatalogOfferResult(
        String offerId,
        String organizationId,
        String productId,
        String variantId,
        String priceId,
        String regionalPolicyReference,
        Instant publishedAt,
        Instant updatedAt) {
}
