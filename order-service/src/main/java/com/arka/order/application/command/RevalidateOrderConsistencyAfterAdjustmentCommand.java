package com.arka.order.application.command;

public record RevalidateOrderConsistencyAfterAdjustmentCommand(
        String tenantId,
        String organizationId,
        String orderId,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
