package com.arka.order.domain.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.domain.cart.enumtype.CheckoutValidationStatus;
import com.arka.order.domain.cart.exception.CartItemInvariantException;
import com.arka.order.domain.cart.exception.InvalidCartStatusTransitionException;
import com.arka.order.domain.cart.valueobject.ValidatedCheckout;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CartAggregateTest {

    @Test
    void shouldCalculateSubtotalAfterAddingItems() {
        Instant now = Instant.parse("2026-04-14T10:00:00Z");
        Cart cart = Cart.create("organization-1", "user-1", now)
                .addOrUpdateItem(
                        null,
                        "variant-1",
                        "SKU-1",
                        2,
                        new BigDecimal("15.00"),
                        "USD",
                        "res-1",
                        true,
                        now)
                .addOrUpdateItem(
                        null,
                        "variant-2",
                        "SKU-2",
                        1,
                        new BigDecimal("5.00"),
                        "USD",
                        "res-2",
                        true,
                        now);

        assertEquals(new BigDecimal("35.00"), cart.subtotal());
    }

    @Test
    void shouldRejectNonPositiveQty() {
        Instant now = Instant.parse("2026-04-14T10:00:00Z");
        Cart cart = Cart.create("organization-1", "user-1", now);

        assertThrows(CartItemInvariantException.class, () -> cart.addOrUpdateItem(
                null,
                "variant-1",
                "SKU-1",
                0,
                new BigDecimal("10.00"),
                "USD",
                "res-1",
                true,
                now));
    }

    @Test
    void shouldPreventMutationsAfterConversion() {
        Instant now = Instant.parse("2026-04-14T10:00:00Z");
        Cart cart = Cart.create("organization-1", "user-1", now)
                .addOrUpdateItem(
                        null,
                        "variant-1",
                        "SKU-1",
                        1,
                        new BigDecimal("10.00"),
                        "USD",
                        "res-1",
                        true,
                        now);

        ValidatedCheckout validatedCheckout = new ValidatedCheckout(
                "corr-1",
                cart.cartId(),
                CheckoutValidationStatus.VALID,
                "organization-1",
                "addr-1",
                "US",
                1L,
                "USD",
                List.of(),
                now);

        Cart converted = cart.markCheckoutValidated(validatedCheckout, now)
                .convertToOrder("order-1", now);

        assertThrows(InvalidCartStatusTransitionException.class, () -> converted.addOrUpdateItem(
                null,
                "variant-2",
                "SKU-2",
                1,
                new BigDecimal("5.00"),
                "USD",
                "res-2",
                true,
                now));
    }
}
