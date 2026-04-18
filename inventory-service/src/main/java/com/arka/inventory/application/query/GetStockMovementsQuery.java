package com.arka.inventory.application.query;

public record GetStockMovementsQuery(
        String organizationId,
        String stockItemId,
        Integer limit,
        String actorUserId) {}
