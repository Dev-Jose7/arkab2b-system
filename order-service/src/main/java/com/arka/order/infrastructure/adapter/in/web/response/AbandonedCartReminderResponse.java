package com.arka.order.infrastructure.adapter.in.web.response;

public record AbandonedCartReminderResponse(
        String message,
        String sourceEventType,
        String channel,
        String recipientRef,
        String cartId) {
}
