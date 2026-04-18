package com.arka.inventory.application.command;

import java.time.Instant;

public record ReserveStockCommand(
        String tenantId,
        String stockItemId,
        String cartId,
        Integer qty,
        Instant expiresAt,
        String actorUserId,
        String idempotencyKey) {}
