package com.arka.order.application.query;

public record GetCartQuery(
        String tenantId,
        String organizationId,
        String cartId,
        String actorUserId) {}
