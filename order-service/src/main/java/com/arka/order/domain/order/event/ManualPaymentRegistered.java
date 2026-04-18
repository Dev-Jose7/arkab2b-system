package com.arka.order.domain.order.event;

import java.time.Instant;

public final class ManualPaymentRegistered extends AbstractOrderDomainEvent {

    private final String paymentReference;

    public ManualPaymentRegistered(
            Instant occurredAt,
            String orderId,
            String paymentReference) {
        super("ManualPaymentRegistered", occurredAt, orderId, "Order");
        this.paymentReference = paymentReference;
    }

    public String paymentReference() {
        return paymentReference;
    }
}
