package com.arka.catalog.application.command;

import java.math.BigDecimal;
import java.time.Instant;

public record RegisterPriceCommand(
        String organizationId,
        String actorId,
        String variantId,
        BigDecimal amount,
        String currency,
        String priceType,
        Instant effectiveFrom,
        Instant effectiveUntil,
        String idempotencyKey) {
}
