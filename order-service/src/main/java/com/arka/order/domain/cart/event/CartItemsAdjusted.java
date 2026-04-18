package com.arka.order.domain.cart.event;

import java.time.Instant;

public final class CartItemsAdjusted extends AbstractCartDomainEvent {

    private final String organizationId;
    private final int itemCount;

    public CartItemsAdjusted(
            Instant occurredAt,
            String cartId,
            String organizationId,

            int itemCount) {
        super("CartItemsAdjusted", occurredAt, cartId, "Cart");
        this.organizationId = organizationId;
        this.itemCount = itemCount;
    }

    public String organizationId() {
        return organizationId;
    }

    public int itemCount() {
        return itemCount;
    }
}
