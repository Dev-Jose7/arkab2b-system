package com.arka.catalog.application.command;

public record PublishCatalogOfferCommand(
        String organizationId,
        String actorId,
        String productId,
        String variantId,
        String priceId,
        String regionalPolicyReference,
        String idempotencyKey) {
}
