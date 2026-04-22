package com.arka.order.infrastructure.adapter.in.web.response;

public record OrderStatusNotificationResponse(
        String message,
        String notificationSourceEventType,
        String notificationRecipientRefHint,
        OrderResponse order) {
}
