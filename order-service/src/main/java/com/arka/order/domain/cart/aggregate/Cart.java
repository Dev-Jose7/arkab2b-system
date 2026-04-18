package com.arka.order.domain.cart.aggregate;

import com.arka.order.domain.cart.entity.CartItem;
import com.arka.order.domain.cart.enumtype.CartStatus;
import com.arka.order.domain.cart.exception.CartItemInvariantException;
import com.arka.order.domain.cart.exception.InvalidCartStatusTransitionException;
import com.arka.order.domain.cart.event.CartCreated;
import com.arka.order.domain.cart.event.CartItemsAdjusted;
import com.arka.order.domain.cart.event.CheckoutAvailabilityValidated;
import com.arka.order.domain.cart.valueobject.ValidatedCheckout;
import com.arka.order.domain.shared.event.DomainEvent;
import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Cart {

    private final String cartId;
    private final String tenantId;
    private final String organizationId;
    private final String userId;
    private final CartStatus status;
    private final long version;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Map<String, CartItem> itemsById;
    private final List<DomainEvent> domainEvents;

    private Cart(
            String cartId,
            String tenantId,
            String organizationId,
            String userId,
            CartStatus status,
            long version,
            Instant createdAt,
            Instant updatedAt,
            Map<String, CartItem> itemsById,
            List<DomainEvent> domainEvents) {
        this.cartId = requireNotBlank(cartId, "cartId");
        this.tenantId = requireNotBlank(tenantId, "tenantId");
        this.organizationId = requireNotBlank(organizationId, "organizationId");
        this.userId = requireNotBlank(userId, "userId");
        this.status = status == null ? CartStatus.ACTIVE : status;
        this.version = version;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        this.itemsById = itemsById == null ? new LinkedHashMap<>() : new LinkedHashMap<>(itemsById);
        this.domainEvents = domainEvents == null ? new ArrayList<>() : domainEvents;
        validate();
    }

    public static Cart create(String tenantId, String organizationId, String userId, Instant now) {
        Instant created = now == null ? Instant.now() : now;
        Cart cart = new Cart(
                UUID.randomUUID().toString(),
                tenantId,
                organizationId,
                userId,
                CartStatus.ACTIVE,
                0,
                created,
                created,
                new LinkedHashMap<>(),
                new ArrayList<>());
        cart.domainEvents.add(new CartCreated(created, cart.cartId, cart.tenantId, cart.organizationId, cart.userId));
        return cart;
    }

    public static Cart rehydrate(
            String cartId,
            String tenantId,
            String organizationId,
            String userId,
            CartStatus status,
            long version,
            Instant createdAt,
            Instant updatedAt,
            List<CartItem> items) {
        Map<String, CartItem> itemMap = new LinkedHashMap<>();
        if (items != null) {
            for (CartItem item : items) {
                itemMap.put(item.cartItemId(), item);
            }
        }
        return new Cart(
                cartId,
                tenantId,
                organizationId,
                userId,
                status,
                version,
                createdAt,
                updatedAt,
                itemMap,
                new ArrayList<>());
    }

    public Cart addOrUpdateItem(
            String cartItemId,
            String variantId,
            String sku,
            int qty,
            BigDecimal unitPrice,
            String currency,
            String reservationId,
            boolean reservationConfirmed,
            Instant now) {
        ensureMutationsAllowed();
        if (qty <= 0) {
            throw new CartItemInvariantException("qty must be positive");
        }
        Instant changedAt = now == null ? Instant.now() : now;

        String itemId = (cartItemId == null || cartItemId.isBlank()) ? UUID.randomUUID().toString() : cartItemId.trim();
        CartItem existing = itemsById.get(itemId);
        CartItem next = existing == null
                ? new CartItem(
                        itemId,
                        cartId,
                        tenantId,
                        organizationId,
                        variantId,
                        sku,
                        qty,
                        unitPrice,
                        currency,
                        reservationId,
                        reservationConfirmed,
                        changedAt,
                        changedAt)
                : existing.adjust(qty, unitPrice, reservationId, reservationConfirmed, changedAt);

        itemsById.put(next.cartItemId(), next);
        domainEvents.add(new CartItemsAdjusted(changedAt, cartId, tenantId, organizationId, itemsById.size()));
        return withVersionIncrement(changedAt);
    }

    public Cart removeItem(String cartItemId, Instant now) {
        ensureMutationsAllowed();
        if (cartItemId == null || cartItemId.isBlank()) {
            throw new CartItemInvariantException("cartItemId is required");
        }
        Instant changedAt = now == null ? Instant.now() : now;
        itemsById.remove(cartItemId);
        domainEvents.add(new CartItemsAdjusted(changedAt, cartId, tenantId, organizationId, itemsById.size()));
        return withVersionIncrement(changedAt);
    }

    public Cart markCheckoutValidated(ValidatedCheckout validatedCheckout, Instant now) {
        ensureMutationsAllowed();
        if (validatedCheckout == null) {
            throw new DomainInvariantViolationException("validatedCheckout is required");
        }
        if (!cartId.equals(validatedCheckout.cartId())) {
            throw new DomainInvariantViolationException("validated checkout cartId mismatch");
        }
        if (!organizationId.equals(validatedCheckout.organizationId())) {
            throw new DomainInvariantViolationException("validated checkout organizationId mismatch");
        }
        Instant changedAt = now == null ? Instant.now() : now;
        domainEvents.add(new CheckoutAvailabilityValidated(
                changedAt,
                cartId,
                tenantId,
                validatedCheckout.checkoutCorrelationId(),
                validatedCheckout.isValid(),
                validatedCheckout.rejectionReasons()));
        return new Cart(
                cartId,
                tenantId,
                organizationId,
                userId,
                validatedCheckout.isValid() ? CartStatus.CHECKOUT_IN_PROGRESS : CartStatus.ACTIVE,
                version + 1,
                createdAt,
                changedAt,
                itemsById,
                domainEvents);
    }

    public Cart convertToOrder(String orderId, Instant now) {
        if (status != CartStatus.CHECKOUT_IN_PROGRESS && status != CartStatus.ACTIVE) {
            throw new InvalidCartStatusTransitionException("Cart cannot be converted from current status");
        }
        requireNotBlank(orderId, "orderId");
        Instant changedAt = now == null ? Instant.now() : now;
        return new Cart(
                cartId,
                tenantId,
                organizationId,
                userId,
                CartStatus.CONVERTED,
                version + 1,
                createdAt,
                changedAt,
                itemsById,
                domainEvents);
    }

    public Cart abandon(Instant now) {
        if (status.isTerminal()) {
            return this;
        }
        Instant changedAt = now == null ? Instant.now() : now;
        return new Cart(
                cartId,
                tenantId,
                organizationId,
                userId,
                CartStatus.ABANDONED,
                version + 1,
                createdAt,
                changedAt,
                itemsById,
                domainEvents);
    }

    public Cart cancel(Instant now) {
        if (status == CartStatus.CONVERTED) {
            throw new InvalidCartStatusTransitionException("Converted cart cannot be cancelled");
        }
        if (status == CartStatus.CANCELLED) {
            return this;
        }
        Instant changedAt = now == null ? Instant.now() : now;
        return new Cart(
                cartId,
                tenantId,
                organizationId,
                userId,
                CartStatus.CANCELLED,
                version + 1,
                createdAt,
                changedAt,
                itemsById,
                domainEvents);
    }

    public void ensureAllItemsHaveConfirmedReservation() {
        if (itemsById.isEmpty()) {
            throw new DomainInvariantViolationException("cart requires at least one item");
        }
        for (CartItem item : itemsById.values()) {
            item.ensureReservationConfirmed();
        }
    }

    public BigDecimal subtotal() {
        return itemsById.values().stream()
                .map(CartItem::lineSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CartItem> items() {
        return List.copyOf(itemsById.values());
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
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

    public String userId() {
        return userId;
    }

    public CartStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private void ensureMutationsAllowed() {
        if (!status.allowsItemMutation()) {
            throw new InvalidCartStatusTransitionException("Cart status does not allow item mutation");
        }
    }

    private Cart withVersionIncrement(Instant changedAt) {
        return new Cart(
                cartId,
                tenantId,
                organizationId,
                userId,
                status,
                version + 1,
                createdAt,
                changedAt,
                itemsById,
                domainEvents);
    }

    private void validate() {
        if (version < 0) {
            throw new DomainInvariantViolationException("version must be non-negative");
        }
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
        return value.trim();
    }
}
