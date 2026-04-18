package com.arka.notification.infrastructure.adapter.in.web.response;

import java.util.List;

public record NotificationTimelineResponse(
        String notificationId,
        List<NotificationTimelineItemResponse> items) {
}
