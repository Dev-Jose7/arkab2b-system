package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;

public record CheckoutVariantResolutionResponse(
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
