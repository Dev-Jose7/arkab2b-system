package com.arka.notification.domain.notificationdispatch.service;

import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;

public final class NotificationKeyFactory {

    private NotificationKeyFactory() {
    }

    public static NotificationKey create(String sourceEventId, String recipientRef, NotificationChannel channel) {
        return NotificationKey.fromEventRecipientAndChannel(sourceEventId, recipientRef, channel);
    }
}
