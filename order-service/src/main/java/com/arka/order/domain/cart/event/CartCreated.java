package com.arka.order.domain.cart.event;

import java.time.Instant;

public final class CartCreated extends AbstractCartDomainEvent {

    private final String tenantId;
    private final String organizationId;
    private final String userId;

    public CartCreated(
            Instant occurredAt,
            String cartId,
            String tenantId,
            String organizationId,
            String userId) {
        super("CartCreated", occurredAt, cartId, "Cart");
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.userId = userId;
    }

    public String tenantId() {
        return tenantId;
    }

    public String organizationId() {
        return organizationId;
    }

    public String userId() {
        return userId;
    }
}
