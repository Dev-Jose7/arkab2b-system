package com.arka.catalog.application.command;

public record ActivateProductCommand(
        String tenantId,
        String actorId,
        String productId,
        String idempotencyKey) {
}
