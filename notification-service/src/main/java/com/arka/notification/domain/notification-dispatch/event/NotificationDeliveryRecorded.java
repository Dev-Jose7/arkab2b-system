package com.arka.notification.domain.notificationdispatch.event;

import java.time.Instant;

public final class NotificationDeliveryRecorded extends AbstractNotificationDomainEvent {

    private final String tenantId;
    private final String attemptId;
    private final String providerCode;
    private final String providerRef;

    public NotificationDeliveryRecorded(
            String notificationId,
            String tenantId,
            String attemptId,
            String providerCode,
            String providerRef,
            Instant occurredAt) {
        super(
                "NotificationDeliveryRecorded",
                "NotificationDispatch",
                notificationId,
                occurredAt);
        this.tenantId = tenantId;
        this.attemptId = attemptId;
        this.providerCode = providerCode;
        this.providerRef = providerRef;
    }

    public String tenantId() {
        return tenantId;
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
