package com.arka.order.domain.cart.enumtype;

public enum CartStatus {
    ACTIVE,
    CHECKOUT_IN_PROGRESS,
    CONVERTED,
    ABANDONED,
    CANCELLED;

    public boolean isTerminal() {
        return this == CONVERTED || this == ABANDONED || this == CANCELLED;
    }

    public boolean allowsItemMutation() {
        return this == ACTIVE || this == CHECKOUT_IN_PROGRESS;
    }
}
