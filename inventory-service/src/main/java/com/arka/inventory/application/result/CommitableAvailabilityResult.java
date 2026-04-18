package com.arka.inventory.application.result;

public record CommitableAvailabilityResult(
        String organizationId,
        String warehouseId,
        String sku,
        int physicalQty,
        int reservedQty,
        int availableQty,
        int reorderPoint,
        int safetyStock,
        boolean lowStock) {}
