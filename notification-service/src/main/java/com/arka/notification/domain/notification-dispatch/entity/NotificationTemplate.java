package com.arka.notification.domain.notificationdispatch.entity;

import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;

public record NotificationTemplate(
        String templateId,
        String organizationId,
        String sourceEventType,
        NotificationChannel channel,
        String subjectTemplate,
        String bodyTemplate,
        boolean active,
        Integer templateVersion) {
}
