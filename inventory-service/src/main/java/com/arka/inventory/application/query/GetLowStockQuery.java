package com.arka.inventory.application.query;

public record GetLowStockQuery(
        String tenantId,
        String warehouseId,
        String actorUserId) {}
