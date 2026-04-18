package com.arka.inventory.application.command;

public record InitializeStockItemCommand(
        String organizationId,
        String warehouseId,
        String sku,
        Integer initialPhysicalQty,
        Integer reorderPoint,
        Integer safetyStock,
        String actorUserId,
        String idempotencyKey) {}
