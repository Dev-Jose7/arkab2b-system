package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record RegisteredStockResponse(
        String stockItemId,
        String warehouseId,
        String sku,
        int physicalQty,
        int reservedQty,
        int availableQty,
        int reorderPoint,
        int safetyStock,
        boolean lowStock,
        String status,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}
