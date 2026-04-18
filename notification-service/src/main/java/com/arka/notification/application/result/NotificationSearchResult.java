package com.arka.notification.application.result;

import java.util.List;

public record NotificationSearchResult(
        List<NotificationSearchItemResult> items,
        int page,
        int size,
        long totalElements) {
}
