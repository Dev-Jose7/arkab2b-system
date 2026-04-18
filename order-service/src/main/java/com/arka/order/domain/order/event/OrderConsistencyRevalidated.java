package com.arka.order.domain.order.event;

import java.time.Instant;

public final class OrderConsistencyRevalidated extends AbstractOrderDomainEvent {

    public OrderConsistencyRevalidated(Instant occurredAt, String orderId) {
        super("OrderConsistencyRevalidated", occurredAt, orderId, "Order");
    }
}
