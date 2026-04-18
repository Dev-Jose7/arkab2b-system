package com.arka.inventory.application.command;

public record UpdateStockItemStatusCommand(
        String tenantId,
        String stockItemId,
        String targetStatus,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
