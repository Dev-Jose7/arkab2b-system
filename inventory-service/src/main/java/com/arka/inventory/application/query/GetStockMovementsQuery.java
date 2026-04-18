package com.arka.inventory.application.query;

public record GetStockMovementsQuery(
        String tenantId,
        String stockItemId,
        Integer limit,
        String actorUserId) {}
