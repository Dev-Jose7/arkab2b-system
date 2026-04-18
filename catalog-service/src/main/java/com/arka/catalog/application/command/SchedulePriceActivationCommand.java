package com.arka.catalog.application.command;

import java.time.Instant;

public record SchedulePriceActivationCommand(
        String tenantId,
        String actorId,
        String priceId,
        Instant executeAfter,
        String idempotencyKey) {
}
