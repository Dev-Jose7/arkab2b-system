package com.arka.order.domain.cart.event;

import java.time.Instant;
import java.util.List;

public final class CheckoutAvailabilityValidated extends AbstractCartDomainEvent {

    private final String tenantId;
    private final String checkoutCorrelationId;
    private final boolean valid;
    private final List<String> reasons;

    public CheckoutAvailabilityValidated(
            Instant occurredAt,
            String cartId,
            String tenantId,
            String checkoutCorrelationId,
            boolean valid,
            List<String> reasons) {
        super("CheckoutAvailabilityValidated", occurredAt, cartId, "Cart");
        this.tenantId = tenantId;
        this.checkoutCorrelationId = checkoutCorrelationId;
        this.valid = valid;
        this.reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public String tenantId() {
        return tenantId;
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
