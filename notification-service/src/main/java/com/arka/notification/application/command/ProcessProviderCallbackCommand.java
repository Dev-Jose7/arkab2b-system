package com.arka.notification.application.command;

public record ProcessProviderCallbackCommand(
        String organizationId,
        String actorId,
        String notificationId,
        String providerCode,
        String providerRef,
        String callbackEventId,
        String callbackStatus,
        String payload,
        String idempotencyKey) {
}
