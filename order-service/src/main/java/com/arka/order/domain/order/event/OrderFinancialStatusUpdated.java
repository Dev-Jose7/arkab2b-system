package com.arka.order.domain.order.event;

import java.time.Instant;

public final class OrderFinancialStatusUpdated extends AbstractOrderDomainEvent {

    private final String financialStatus;

    public OrderFinancialStatusUpdated(Instant occurredAt, String orderId, String financialStatus) {
        super("OrderFinancialStatusUpdated", occurredAt, orderId, "Order");
        this.financialStatus = financialStatus;
    }

    public String financialStatus() {
        return financialStatus;
    }
}
