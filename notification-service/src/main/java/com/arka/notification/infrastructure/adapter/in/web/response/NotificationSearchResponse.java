package com.arka.notification.infrastructure.adapter.in.web.response;

import java.util.List;

public record NotificationSearchResponse(
        List<NotificationSearchItemResponse> items,
        int page,
        int size,
        long totalElements) {
}
