package com.arka.catalog.application.command;

import java.time.Instant;

public record ChangeVariantStatusCommand(
        String organizationId,
        String actorId,
        String variantId,
        String targetStatus,
        Instant sellableFrom,
        Instant sellableUntil,
        String idempotencyKey) {
}
