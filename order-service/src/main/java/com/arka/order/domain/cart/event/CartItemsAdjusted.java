package com.arka.order.domain.cart.event;

import java.time.Instant;

public final class CartItemsAdjusted extends AbstractCartDomainEvent {

    private final String tenantId;
    private final String organizationId;
    private final int itemCount;

    public CartItemsAdjusted(
            Instant occurredAt,
            String cartId,
            String tenantId,
            String organizationId,
            int itemCount) {
        super("CartItemsAdjusted", occurredAt, cartId, "Cart");
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.itemCount = itemCount;
    }

    public String tenantId() {
        return tenantId;
    }

    public String organizationId() {
        return organizationId;
    }

    public int itemCount() {
        return itemCount;
    }
}
