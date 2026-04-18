package com.arka.order.application.query;

public record GetCartQuery(
        String organizationId,

        String cartId,
        String actorUserId) {}
