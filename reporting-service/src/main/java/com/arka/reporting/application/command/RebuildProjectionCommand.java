package com.arka.reporting.application.command;

public record RebuildProjectionCommand(
        String organizationId,
        String actorId,
        boolean fullRebuild,
        String weekId,
        String idempotencyKey) {
}
