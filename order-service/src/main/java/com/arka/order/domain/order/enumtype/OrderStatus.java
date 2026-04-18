package com.arka.order.domain.order.enumtype;

import java.util.Set;

public enum OrderStatus {
    CREATED,
    PENDING_APPROVAL,
    CONFIRMED,
    CANCELLED,
    READY_TO_DISPATCH,
    DISPATCHED,
    DELIVERED;

    public boolean isTerminal() {
        return this == CANCELLED || this == DELIVERED;
    }

    public boolean allowsAdjustmentBeforeClose() {
        return this == CREATED || this == PENDING_APPROVAL;
    }

    public boolean canTransitionTo(OrderStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case CREATED -> Set.of(PENDING_APPROVAL, CANCELLED).contains(target);
            case PENDING_APPROVAL -> Set.of(CONFIRMED, CANCELLED).contains(target);
            case CONFIRMED -> Set.of(CANCELLED, READY_TO_DISPATCH).contains(target);
            case READY_TO_DISPATCH -> Set.of(DISPATCHED).contains(target);
            case DISPATCHED -> Set.of(DELIVERED).contains(target);
            case CANCELLED, DELIVERED -> false;
        };
    }
}
