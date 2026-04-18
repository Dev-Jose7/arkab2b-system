package com.arka.inventory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record StockMovementResponse(
        String movementId,
        String tenantId,
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
