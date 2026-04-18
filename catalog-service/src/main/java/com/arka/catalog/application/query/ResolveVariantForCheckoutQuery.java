package com.arka.catalog.application.query;

import java.time.Instant;

public record ResolveVariantForCheckoutQuery(
        String tenantId,
        String sku,
        String currency,
        String priceType,
        Instant at) {
}
