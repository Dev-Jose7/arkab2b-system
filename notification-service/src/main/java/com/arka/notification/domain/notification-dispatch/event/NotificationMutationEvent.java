package com.arka.notification.domain.notificationdispatch.event;

import java.time.Instant;

public final class NotificationMutationEvent extends AbstractNotificationDomainEvent {

    public NotificationMutationEvent(String aggregateType, String aggregateId, String eventType, Instant occurredAt) {
        super(eventType, aggregateType, aggregateId, occurredAt);
    }
}
