package com.arka.notification.domain.notificationdispatch.entity;

import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;

public record ChannelPolicy(
        String policyId,
        String tenantId,
        String sourceEventType,
        NotificationChannel primaryChannel,
        NotificationChannel fallbackChannel,
        int maxAttempts,
        int retryIntervalSeconds,
        boolean active) {

    public ChannelPolicy {
        maxAttempts = Math.max(maxAttempts, 1);
        retryIntervalSeconds = Math.max(retryIntervalSeconds, 0);
    }
}
