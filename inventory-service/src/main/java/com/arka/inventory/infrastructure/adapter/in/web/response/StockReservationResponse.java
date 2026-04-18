package com.arka.inventory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record StockReservationResponse(
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
