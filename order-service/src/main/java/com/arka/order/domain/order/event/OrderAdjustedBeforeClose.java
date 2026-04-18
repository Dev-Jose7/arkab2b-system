package com.arka.order.domain.order.event;

import java.time.Instant;

public final class OrderAdjustedBeforeClose extends AbstractOrderDomainEvent {

    public OrderAdjustedBeforeClose(Instant occurredAt, String orderId) {
        super("OrderAdjustedBeforeClose", occurredAt, orderId, "Order");
    }
}
