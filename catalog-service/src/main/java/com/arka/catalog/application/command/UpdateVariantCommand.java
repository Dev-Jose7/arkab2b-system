package com.arka.catalog.application.command;

public record UpdateVariantCommand(
        String organizationId,
        String actorId,
        String variantId,
        String name,
        String description,
        Integer weightGrams,
        String idempotencyKey) {
}
