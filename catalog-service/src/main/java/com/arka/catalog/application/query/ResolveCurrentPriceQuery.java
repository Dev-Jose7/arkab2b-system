package com.arka.catalog.application.query;

import java.time.Instant;

public record ResolveCurrentPriceQuery(
        String organizationId,
        String variantId,
        String currency,
        String priceType,
        Instant at) {
}
