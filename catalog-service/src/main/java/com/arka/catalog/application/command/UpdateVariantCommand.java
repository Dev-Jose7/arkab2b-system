package com.arka.catalog.application.command;

public record UpdateVariantCommand(
        String tenantId,
        String actorId,
        String variantId,
        String name,
        String description,
        Integer weightGrams,
        String idempotencyKey) {
}
