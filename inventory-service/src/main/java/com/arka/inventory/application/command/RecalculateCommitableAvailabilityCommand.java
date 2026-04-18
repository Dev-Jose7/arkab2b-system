package com.arka.inventory.application.command;

public record RecalculateCommitableAvailabilityCommand(
        String organizationId,
        String stockItemId,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
