package com.arka.catalog.application.command;

public record RetireProductCommand(
        String tenantId,
        String actorId,
        String productId,
        String idempotencyKey) {
}
