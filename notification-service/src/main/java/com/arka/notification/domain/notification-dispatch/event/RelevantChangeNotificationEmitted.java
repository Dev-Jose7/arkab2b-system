package com.arka.notification.domain.notificationdispatch.event;

import java.time.Instant;

public final class RelevantChangeNotificationEmitted extends AbstractNotificationDomainEvent {

    private final String tenantId;
    private final String sourceEventId;
    private final String sourceEventType;
    private final String recipientRef;
    private final String channel;

    public RelevantChangeNotificationEmitted(
            String notificationId,
            String tenantId,
            String sourceEventId,
            String sourceEventType,
            String recipientRef,
            String channel,
            Instant occurredAt) {
        super(
                "RelevantChangeNotificationEmitted",
                "NotificationDispatch",
                notificationId,
                occurredAt);
        this.tenantId = tenantId;
        this.sourceEventId = sourceEventId;
        this.sourceEventType = sourceEventType;
        this.recipientRef = recipientRef;
        this.channel = channel;
    }

    public String tenantId() {
        return tenantId;
    }

    public String sourceEventId() {
        return sourceEventId;
    }

    public String sourceEventType() {
        return sourceEventType;
    }

    public String recipientRef() {
        return recipientRef;
    }

    public String channel() {
        return channel;
    }
}
