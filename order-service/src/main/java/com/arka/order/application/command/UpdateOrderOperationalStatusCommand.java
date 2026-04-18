package com.arka.order.application.command;

public record UpdateOrderOperationalStatusCommand(
        String tenantId,
        String organizationId,
        String orderId,
        String targetStatus,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
