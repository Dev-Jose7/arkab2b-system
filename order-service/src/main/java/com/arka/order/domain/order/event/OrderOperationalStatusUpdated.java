package com.arka.order.domain.order.event;

import java.time.Instant;

public final class OrderOperationalStatusUpdated extends AbstractOrderDomainEvent {

    private final String fromStatus;
    private final String toStatus;

    public OrderOperationalStatusUpdated(
            Instant occurredAt,
            String orderId,
            String fromStatus,
            String toStatus) {
        super("OrderOperationalStatusUpdated", occurredAt, orderId, "Order");
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public String fromStatus() {
        return fromStatus;
    }

    public String toStatus() {
        return toStatus;
    }
}
