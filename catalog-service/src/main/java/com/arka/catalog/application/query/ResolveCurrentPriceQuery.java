package com.arka.catalog.application.query;

import java.time.Instant;

public record ResolveCurrentPriceQuery(
        String tenantId,
        String variantId,
        String currency,
        String priceType,
        Instant at) {
}
