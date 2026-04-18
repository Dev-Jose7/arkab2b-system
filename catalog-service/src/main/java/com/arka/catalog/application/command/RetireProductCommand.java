package com.arka.catalog.application.command;

public record RetireProductCommand(
        String organizationId,
        String actorId,
        String productId,
        String idempotencyKey) {
}
