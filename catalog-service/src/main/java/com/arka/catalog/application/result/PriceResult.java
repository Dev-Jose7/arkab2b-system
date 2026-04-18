package com.arka.catalog.application.result;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceResult(
        String priceId,
        String organizationId,
        String variantId,
        String priceType,
        BigDecimal amount,
        String currency,
        String status,
        Instant effectiveFrom,
        Instant effectiveUntil,
        Instant createdAt,
        Instant updatedAt) {
}
