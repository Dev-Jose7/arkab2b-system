package com.arka.order.application.command;

public record UpdateOrderOperationalStatusCommand(
        String organizationId,

        String orderId,
        String targetStatus,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
