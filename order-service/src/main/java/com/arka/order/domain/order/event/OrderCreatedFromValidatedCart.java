package com.arka.order.domain.order.event;

import java.time.Instant;

public final class OrderCreatedFromValidatedCart extends AbstractOrderDomainEvent {

    private final String tenantId;
    private final String cartId;
    private final String orderNumber;

    public OrderCreatedFromValidatedCart(
            Instant occurredAt,
            String orderId,
            String tenantId,
            String cartId,
            String orderNumber) {
        super("OrderCreatedFromValidatedCart", occurredAt, orderId, "Order");
        this.tenantId = tenantId;
        this.cartId = cartId;
        this.orderNumber = orderNumber;
    }

    public String tenantId() {
        return tenantId;
    }

    public String cartId() {
        return cartId;
    }

    public String orderNumber() {
        return orderNumber;
    }
}
