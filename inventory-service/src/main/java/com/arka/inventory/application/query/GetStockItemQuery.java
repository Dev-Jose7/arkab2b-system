package com.arka.inventory.application.query;

public record GetStockItemQuery(
        String tenantId,
        String stockItemId,
        String actorUserId) {}
