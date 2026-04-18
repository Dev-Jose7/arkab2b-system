package com.arka.inventory.application.result;

import java.time.Instant;

public record StockItemResult(
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
