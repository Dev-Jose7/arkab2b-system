package com.arka.inventory.application.query;

public record ListReservationsByCartQuery(
        String tenantId,
        String cartId,
        String actorUserId) {}
