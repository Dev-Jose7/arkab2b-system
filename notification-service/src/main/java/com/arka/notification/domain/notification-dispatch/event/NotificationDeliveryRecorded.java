package com.arka.notification.domain.notificationdispatch.event;

import java.time.Instant;

public final class NotificationDeliveryRecorded extends AbstractNotificationDomainEvent {

    private final String organizationId;
    private final String attemptId;
    private final String providerCode;
    private final String providerRef;

    public NotificationDeliveryRecorded(
            String notificationId,
            String organizationId,
            String attemptId,
            String providerCode,
            String providerRef,
            Instant occurredAt) {
        super(
                "NotificationDeliveryRecorded",
                "NotificationDispatch",
                notificationId,
                occurredAt);
        this.organizationId = organizationId;
        this.attemptId = attemptId;
        this.providerCode = providerCode;
        this.providerRef = providerRef;
    }

    public String organizationId() {
        return organizationId;
    }

    public String attemptId() {
        return attemptId;
    }

    public String providerCode() {
        return providerCode;
    }

    public String providerRef() {
        return providerRef;
    }
}
