package com.arka.inventory.application.command;

public record UpdateStockItemStatusCommand(
        String organizationId,
        String stockItemId,
        String targetStatus,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
