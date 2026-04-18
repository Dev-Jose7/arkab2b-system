package com.arka.notification.application.result;

import java.util.List;

public record NotificationTimelineResult(
        String notificationId,
        List<NotificationTimelineItemResult> items) {
}
