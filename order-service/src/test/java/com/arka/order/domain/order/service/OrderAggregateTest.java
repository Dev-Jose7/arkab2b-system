package com.arka.order.domain.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.domain.order.entity.OrderLine;
import com.arka.order.domain.order.enumtype.FinancialStatus;
import com.arka.order.domain.order.enumtype.OrderStatus;
import com.arka.order.domain.order.exception.InvalidOrderTransitionException;
import com.arka.order.domain.order.exception.ManualPaymentException;
import com.arka.order.domain.order.exception.OrderConsistencyException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderAggregateTest {

    @Test
    void shouldCreateOrderOnlyWithConfirmedReservations() {
        Instant now = Instant.parse("2026-04-14T11:00:00Z");
        OrderLine line = new OrderLine(
                UUID.randomUUID().toString(),
                "PENDING_ORDER_ID",
                "tenant-1",
                "org-1",
                "variant-1",
                "SKU-1",
                2,
                new BigDecimal("12.00"),
                "USD",
                "res-1",
                true,
                null,
                now,
                now);

        Order order = Order.createFromValidatedCart(
                "tenant-1",
                "org-1",
                "user-1",
                "cart-1",
                "corr-1",
                "addr-1",
                "US",
                1L,
                "USD",
                List.of(line),
                now);

        assertEquals(OrderStatus.PENDING_APPROVAL, order.status());
        assertEquals(FinancialStatus.PENDING, order.financialStatus());
        assertEquals(1, order.lines().size());
    }

    @Test
    void shouldApplyValidStatusTransition() {
        Order order = sampleOrder();

        Order confirmed = order.updateOperationalStatus(OrderStatus.CONFIRMED, "approved", Instant.now());

        assertEquals(OrderStatus.CONFIRMED, confirmed.status());
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Order order = sampleOrder().updateOperationalStatus(OrderStatus.CANCELLED, "cancel", Instant.now());

        assertThrows(InvalidOrderTransitionException.class,
                () -> order.updateOperationalStatus(OrderStatus.CONFIRMED, "invalid", Instant.now()));
    }

    @Test
    void shouldRejectDuplicatedPaymentReferenceInSameOrder() {
        Order order = sampleOrder().registerManualPayment(
                "PAY-1",
                new BigDecimal("5.00"),
                "TRANSFER",
                "SUP-1",
                Instant.now(),
                Instant.now());

        assertThrows(ManualPaymentException.class,
                () -> order.registerManualPayment(
                        "PAY-1",
                        new BigDecimal("5.00"),
                        "TRANSFER",
                        "SUP-2",
                        Instant.now(),
                        Instant.now()));
    }

    @Test
    void shouldRejectManualPaymentAmountLessOrEqualThanZero() {
        Order order = sampleOrder();

        assertThrows(ManualPaymentException.class,
                () -> order.registerManualPayment(
                        "PAY-1",
                        BigDecimal.ZERO,
                        "TRANSFER",
                        "SUP-1",
                        Instant.now(),
                        Instant.now()));
    }

    @Test
    void shouldRejectOrderLineWhenReservationIsNotConfirmed() {
        Instant now = Instant.parse("2026-04-14T11:00:00Z");
        assertThrows(OrderConsistencyException.class, () -> new OrderLine(
                UUID.randomUUID().toString(),
                "PENDING_ORDER_ID",
                "tenant-1",
                "org-1",
                "variant-1",
                "SKU-1",
                1,
                new BigDecimal("12.00"),
                "USD",
                "res-1",
                false,
                null,
                now,
                now));
    }

    @Test
    void shouldRejectAdjustBeforeCloseAfterOrderIsConfirmed() {
        Instant now = Instant.parse("2026-04-14T11:00:00Z");
        Order confirmed = sampleOrder().updateOperationalStatus(OrderStatus.CONFIRMED, "approved", now.plusSeconds(5));

        assertThrows(InvalidOrderTransitionException.class, () -> confirmed.adjustBeforeClose(confirmed.lines(), now.plusSeconds(10)));
    }

    private Order sampleOrder() {
        Instant now = Instant.parse("2026-04-14T11:00:00Z");
        OrderLine line = new OrderLine(
                UUID.randomUUID().toString(),
                "PENDING_ORDER_ID",
                "tenant-1",
                "org-1",
                "variant-1",
                "SKU-1",
                2,
                new BigDecimal("12.00"),
                "USD",
                "res-1",
                true,
                null,
                now,
                now);

        return Order.createFromValidatedCart(
                "tenant-1",
                "org-1",
                "user-1",
                "cart-1",
                "corr-1",
                "addr-1",
                "US",
                1L,
                "USD",
                List.of(line),
                now);
    }
}
