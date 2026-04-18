package com.arka.inventory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record StockItemResponse(
        String stockItemId,
        String organizationId,
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
        Instant updatedAt) {}
