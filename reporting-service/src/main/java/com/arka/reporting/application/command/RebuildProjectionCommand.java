package com.arka.reporting.application.command;

public record RebuildProjectionCommand(
        String tenantId,
        String actorId,
        boolean fullRebuild,
        String weekId,
        String idempotencyKey) {
}
