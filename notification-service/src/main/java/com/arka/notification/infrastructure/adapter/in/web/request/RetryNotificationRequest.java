package com.arka.notification.infrastructure.adapter.in.web.request;

public record RetryNotificationRequest(String idempotencyKey) {
}
