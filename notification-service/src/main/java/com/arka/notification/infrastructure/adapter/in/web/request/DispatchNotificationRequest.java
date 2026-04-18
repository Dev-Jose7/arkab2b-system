package com.arka.notification.infrastructure.adapter.in.web.request;

public record DispatchNotificationRequest(String idempotencyKey) {
}
