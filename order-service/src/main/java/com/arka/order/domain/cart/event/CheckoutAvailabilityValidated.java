package com.arka.order.domain.cart.event;

import java.time.Instant;
import java.util.List;

public final class CheckoutAvailabilityValidated extends AbstractCartDomainEvent {

    private final String organizationId;
    private final String checkoutCorrelationId;
    private final boolean valid;
    private final List<String> reasons;

    public CheckoutAvailabilityValidated(
            Instant occurredAt,
            String cartId,
            String organizationId,
            String checkoutCorrelationId,
            boolean valid,
            List<String> reasons) {
        super("CheckoutAvailabilityValidated", occurredAt, cartId, "Cart");
        this.organizationId = organizationId;
        this.checkoutCorrelationId = checkoutCorrelationId;
        this.valid = valid;
        this.reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public String organizationId() {
        return organizationId;
    }

    public String checkoutCorrelationId() {
        return checkoutCorrelationId;
    }

    public boolean valid() {
        return valid;
    }

    public List<String> reasons() {
        return reasons;
    }
}
