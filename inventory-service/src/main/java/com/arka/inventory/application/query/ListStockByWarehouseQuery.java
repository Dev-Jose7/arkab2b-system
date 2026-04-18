package com.arka.inventory.application.query;

public record ListStockByWarehouseQuery(
        String tenantId,
        String warehouseId,
        String actorUserId) {}
