package com.arka.catalog.application.command;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdatePriceCommand(
        String tenantId,
        String actorId,
        String priceId,
        BigDecimal amount,
        String currency,
        String priceType,
        Instant effectiveFrom,
        Instant effectiveUntil,
        String idempotencyKey) {
}
