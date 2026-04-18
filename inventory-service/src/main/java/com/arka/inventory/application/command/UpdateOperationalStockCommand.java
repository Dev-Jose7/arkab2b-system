package com.arka.inventory.application.command;

public record UpdateOperationalStockCommand(
        String organizationId,
        String stockItemId,
        Integer deltaQty,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
