package com.arka.inventory.application.query;

public record GetCommitableAvailabilityQuery(
        String organizationId,
        String warehouseId,
        String sku,
        String actorUserId) {}
