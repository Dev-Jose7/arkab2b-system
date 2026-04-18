package com.arka.inventory.infrastructure.adapter.in.web.response;

public record CommitableAvailabilityResponse(
        String tenantId,
        String warehouseId,
        String sku,
        int physicalQty,
        int reservedQty,
        int availableQty,
        int reorderPoint,
        int safetyStock,
        boolean lowStock) {}
