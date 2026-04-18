package com.arka.order.application.command;

public record CreateCartCommand(
        String tenantId,
        String organizationId,
        String userId,
        String actorUserId,
        String idempotencyKey) {}
