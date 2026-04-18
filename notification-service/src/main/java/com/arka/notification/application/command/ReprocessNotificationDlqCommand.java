package com.arka.notification.application.command;

public record ReprocessNotificationDlqCommand(
        String tenantId,
        String actorId,
        String notificationId,
        String dlqEventId,
        String consumerName,
        String idempotencyKey) {
}
