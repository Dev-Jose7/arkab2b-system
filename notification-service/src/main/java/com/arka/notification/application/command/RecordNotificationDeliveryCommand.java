package com.arka.notification.application.command;

public record RecordNotificationDeliveryCommand(
        String organizationId,
        String actorId,
        String notificationId,
        String attemptId,
        String providerCode,
        String providerRef,
        String idempotencyKey) {
}
