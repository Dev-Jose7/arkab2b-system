package com.arka.catalog.application.result;

import java.math.BigDecimal;
import java.time.Instant;

public record CheckoutVariantResolutionResult(
        String tenantId,
        String productId,
        String variantId,
        String sku,
        String priceId,
        BigDecimal amount,
        String currency,
        String priceType,
        Instant resolvedAt) {
}
