package com.arka.reporting.infrastructure.adapter.in.web.request;

public record RebuildProjectionRequest(
        boolean fullRebuild,
        String weekId,
        String idempotencyKey) {
}
