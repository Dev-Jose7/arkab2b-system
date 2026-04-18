package com.arka.inventory.application.command;

public record RecalculateCommitableAvailabilityCommand(
        String tenantId,
        String stockItemId,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
