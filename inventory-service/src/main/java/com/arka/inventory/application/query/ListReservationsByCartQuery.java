package com.arka.inventory.application.query;

public record ListReservationsByCartQuery(
        String organizationId,
        String cartId,
        String actorUserId) {}
