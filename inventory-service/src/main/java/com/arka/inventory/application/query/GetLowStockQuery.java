package com.arka.inventory.application.query;

public record GetLowStockQuery(
        String organizationId,
        String warehouseId,
        String actorUserId) {}
