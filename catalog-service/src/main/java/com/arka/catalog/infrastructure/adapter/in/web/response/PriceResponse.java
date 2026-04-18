package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceResponse(
        String priceId,
        String tenantId,
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
