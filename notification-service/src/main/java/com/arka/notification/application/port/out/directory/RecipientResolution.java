package com.arka.notification.application.port.out.directory;

public record RecipientResolution(
        String recipientRef,
        String channel,
        String destination,
        boolean active) {
}
