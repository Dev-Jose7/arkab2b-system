package com.arka.inventory.application.query;

public record GetStockItemQuery(
        String organizationId,
        String stockItemId,
        String actorUserId) {}
