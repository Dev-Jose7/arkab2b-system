package com.arka.order.infrastructure.adapter.in.web.request;

public record AbandonedCartReminderRequest(
        String channel,
        String note) {

    public String channel() {
        return channel == null || channel.isBlank() ? "EMAIL" : channel.trim().toUpperCase();
    }
}
