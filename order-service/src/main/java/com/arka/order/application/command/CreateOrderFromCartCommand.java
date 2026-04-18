package com.arka.order.application.command;

public record CreateOrderFromCartCommand(
        String organizationId,

        String userId,
        String cartId,
        String checkoutCorrelationId,
        String actorUserId,
        String idempotencyKey) {}
