package com.arka.notification.application.command;

public record ProcessProviderCallbackCommand(
        String tenantId,
        String actorId,
        String notificationId,
        String providerCode,
        String providerRef,
        String callbackEventId,
        String callbackStatus,
        String payload,
        String idempotencyKey) {
}
