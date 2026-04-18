package com.arka.order.domain.cart.entity;

import com.arka.order.domain.cart.exception.CartItemInvariantException;
import java.math.BigDecimal;
import java.time.Instant;

public final class CartItem {

    private final String cartItemId;
    private final String cartId;
    private final String tenantId;
    private final String organizationId;
    private final String variantId;
    private final String sku;
    private final int qty;
    private final BigDecimal unitPrice;
    private final String currency;
    private final String reservationId;
    private final boolean reservationConfirmed;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CartItem(
            String cartItemId,
            String cartId,
            String tenantId,
            String organizationId,
            String variantId,
            String sku,
            int qty,
            BigDecimal unitPrice,
            String currency,
            String reservationId,
            boolean reservationConfirmed,
            Instant createdAt,
            Instant updatedAt) {
        this.cartItemId = requireNotBlank(cartItemId, "cartItemId");
        this.cartId = requireNotBlank(cartId, "cartId");
        this.tenantId = requireNotBlank(tenantId, "tenantId");
        this.organizationId = requireNotBlank(organizationId, "organizationId");
        this.variantId = requireNotBlank(variantId, "variantId");
        this.sku = requireNotBlank(sku, "sku").toUpperCase();
        this.qty = qty;
        this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        this.currency = requireNotBlank(currency, "currency").toUpperCase();
        this.reservationId = reservationId == null || reservationId.isBlank() ? null : reservationId.trim();
        this.reservationConfirmed = reservationConfirmed;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        validate();
    }

    public CartItem adjust(
            int nextQty,
            BigDecimal nextUnitPrice,
            String nextReservationId,
            boolean nextReservationConfirmed,
            Instant now) {
        return new CartItem(
                cartItemId,
                cartId,
                tenantId,
                organizationId,
                variantId,
                sku,
                nextQty,
                nextUnitPrice == null ? unitPrice : nextUnitPrice,
                currency,
                nextReservationId,
                nextReservationConfirmed,
                createdAt,
                now == null ? Instant.now() : now);
    }

    public BigDecimal lineSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }

    public void ensureReservationConfirmed() {
        if (reservationId == null || reservationId.isBlank()) {
            throw new CartItemInvariantException("cart item requires reservation_id");
        }
        if (!reservationConfirmed) {
            throw new CartItemInvariantException("cart item reservation is not confirmed");
        }
    }

    private void validate() {
        if (qty <= 0) {
            throw new CartItemInvariantException("cart item qty must be positive");
        }
        if (unitPrice.signum() < 0) {
            throw new CartItemInvariantException("cart item unitPrice cannot be negative");
        }
        if (reservationConfirmed && (reservationId == null || reservationId.isBlank())) {
            throw new CartItemInvariantException("reservationConfirmed requires reservationId");
        }
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new CartItemInvariantException(fieldName + " is required");
        }
        return value.trim();
    }

    public String cartItemId() {
        return cartItemId;
    }

    public String cartId() {
        return cartId;
    }

    public String tenantId() {
        return tenantId;
    }

    public String organizationId() {
        return organizationId;
    }

    public String variantId() {
        return variantId;
    }

    public String sku() {
        return sku;
    }

    public int qty() {
        return qty;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public String currency() {
        return currency;
    }

    public String reservationId() {
        return reservationId;
    }

    public boolean reservationConfirmed() {
        return reservationConfirmed;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
