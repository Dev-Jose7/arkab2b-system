package com.arka.inventory.application.result;

import java.time.Instant;

public record StockReservationResult(
        String reservationId,
        String tenantId,
        String stockItemId,
        String warehouseId,
        String sku,
        String cartId,
        String orderId,
        int qty,
        String status,
        Instant expiresAt,
        Instant confirmedAt,
        Instant releasedAt,
        Instant createdAt,
        Instant updatedAt) {}
