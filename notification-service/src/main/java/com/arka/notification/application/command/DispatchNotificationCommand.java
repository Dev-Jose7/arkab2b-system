package com.arka.notification.application.command;

public record DispatchNotificationCommand(
        String organizationId,
        String actorId,
        String notificationId,
        String idempotencyKey) {
}
