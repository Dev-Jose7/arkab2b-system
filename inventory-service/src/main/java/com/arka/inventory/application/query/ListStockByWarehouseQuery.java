package com.arka.inventory.application.query;

public record ListStockByWarehouseQuery(
        String organizationId,
        String warehouseId,
        String actorUserId) {}
