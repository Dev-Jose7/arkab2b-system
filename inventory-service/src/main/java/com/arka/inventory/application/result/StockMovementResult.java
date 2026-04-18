package com.arka.inventory.application.result;

import java.time.Instant;

public record StockMovementResult(
        String movementId,
        String organizationId,
        String stockItemId,
        String warehouseId,
        String sku,
        String movementType,
        int deltaQty,
        String reason,
        String reservationId,
        String orderId,
        String correlationId,
        Instant createdAt) {}
