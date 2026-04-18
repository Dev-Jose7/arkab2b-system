package com.arka.order.application.command;

public record CancelOrderCommand(
        String organizationId,

        String orderId,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
