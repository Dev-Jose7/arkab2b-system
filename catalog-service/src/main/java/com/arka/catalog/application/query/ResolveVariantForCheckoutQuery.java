package com.arka.catalog.application.query;

import java.time.Instant;

public record ResolveVariantForCheckoutQuery(
        String organizationId,
        String sku,
        String currency,
        String priceType,
        Instant at) {
}
