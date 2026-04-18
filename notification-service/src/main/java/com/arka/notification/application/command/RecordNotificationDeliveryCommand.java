package com.arka.notification.application.command;

public record RecordNotificationDeliveryCommand(
        String tenantId,
        String actorId,
        String notificationId,
        String attemptId,
        String providerCode,
        String providerRef,
        String idempotencyKey) {
}
