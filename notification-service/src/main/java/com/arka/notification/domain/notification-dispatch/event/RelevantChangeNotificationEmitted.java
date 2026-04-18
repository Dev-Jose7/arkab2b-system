package com.arka.notification.domain.notificationdispatch.event;

import java.time.Instant;

public final class RelevantChangeNotificationEmitted extends AbstractNotificationDomainEvent {

    private final String organizationId;
    private final String sourceEventId;
    private final String sourceEventType;
    private final String recipientRef;
    private final String channel;

    public RelevantChangeNotificationEmitted(
            String notificationId,
            String organizationId,
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
        this.organizationId = organizationId;
        this.sourceEventId = sourceEventId;
        this.sourceEventType = sourceEventType;
        this.recipientRef = recipientRef;
        this.channel = channel;
    }

    public String organizationId() {
        return organizationId;
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
