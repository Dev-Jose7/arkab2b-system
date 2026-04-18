package com.arka.notification.application.result;

import java.util.List;

public record NotificationAuditResult(
        List<NotificationAuditEntryResult> entries,
        int page,
        int size,
        long totalElements) {
}
