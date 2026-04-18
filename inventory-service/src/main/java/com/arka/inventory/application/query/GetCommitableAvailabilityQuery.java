package com.arka.inventory.application.query;

public record GetCommitableAvailabilityQuery(
        String tenantId,
        String warehouseId,
        String sku,
        String actorUserId) {}
