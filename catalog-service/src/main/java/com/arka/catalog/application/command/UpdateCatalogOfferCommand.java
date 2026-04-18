package com.arka.catalog.application.command;

public record UpdateCatalogOfferCommand(
        String tenantId,
        String actorId,
        String offerId,
        String variantId,
        String priceId,
        String regionalPolicyReference,
        String idempotencyKey) {
}
