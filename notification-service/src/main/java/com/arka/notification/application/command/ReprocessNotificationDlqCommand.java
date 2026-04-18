package com.arka.notification.application.command;

public record ReprocessNotificationDlqCommand(
        String organizationId,
        String actorId,
        String notificationId,
        String dlqEventId,
        String consumerName,
        String idempotencyKey) {
}
