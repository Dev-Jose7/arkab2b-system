package com.arka.notification.infrastructure.adapter.in.web.response;

import java.util.List;

public record NotificationAuditResponse(
        List<NotificationAuditEntryResponse> entries,
        int page,
        int size,
        long totalElements) {
}
