package com.arka.order.domain.order.aggregate;

import com.arka.order.domain.order.entity.ManualPayment;
import com.arka.order.domain.order.entity.OrderLine;
import com.arka.order.domain.order.enumtype.FinancialStatus;
import com.arka.order.domain.order.enumtype.ManualPaymentStatus;
import com.arka.order.domain.order.enumtype.OrderStatus;
import com.arka.order.domain.order.event.ManualPaymentRegistered;
import com.arka.order.domain.order.event.OrderAdjustedBeforeClose;
import com.arka.order.domain.order.event.OrderConsistencyRevalidated;
import com.arka.order.domain.order.event.OrderCreatedFromValidatedCart;
import com.arka.order.domain.order.event.OrderFinancialStatusUpdated;
import com.arka.order.domain.order.event.OrderOperationalStatusUpdated;
import com.arka.order.domain.order.exception.InvalidOrderTransitionException;
import com.arka.order.domain.order.exception.ManualPaymentException;
import com.arka.order.domain.order.exception.OrderConsistencyException;
import com.arka.order.domain.shared.event.DomainEvent;
import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Order {

    private final String orderId;
    private final String orderNumber;
    private final String organizationId;
    private final String userId;
    private final String cartId;
    private final String checkoutCorrelationId;
    private final String addressId;
    private final String countryCode;
    private final long regionalPolicyVersion;
    private final String policyCurrency;
    private final OrderStatus status;
    private final FinancialStatus financialStatus;
    private final BigDecimal subtotal;
    private final BigDecimal totalAmount;
    private final long version;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Map<String, OrderLine> linesById;
    private final Map<String, ManualPayment> paymentsById;
    private final List<DomainEvent> domainEvents;

    private Order(
            String orderId,
            String orderNumber,
            String organizationId,

            String userId,
            String cartId,
            String checkoutCorrelationId,
            String addressId,
            String countryCode,
            long regionalPolicyVersion,
            String policyCurrency,
            OrderStatus status,
            FinancialStatus financialStatus,
            BigDecimal subtotal,
            BigDecimal totalAmount,
            long version,
            Instant createdAt,
            Instant updatedAt,
            Map<String, OrderLine> linesById,
            Map<String, ManualPayment> paymentsById,
            List<DomainEvent> domainEvents) {
        this.orderId = requireNotBlank(orderId, "orderId");
        this.orderNumber = requireNotBlank(orderNumber, "orderNumber");
        this.organizationId = requireNotBlank(organizationId, "organizationId");
        this.userId = requireNotBlank(userId, "userId");
        this.cartId = requireNotBlank(cartId, "cartId");
        this.checkoutCorrelationId = requireNotBlank(checkoutCorrelationId, "checkoutCorrelationId");
        this.addressId = requireNotBlank(addressId, "addressId");
        this.countryCode = requireNotBlank(countryCode, "countryCode").toUpperCase();
        this.regionalPolicyVersion = regionalPolicyVersion;
        this.policyCurrency = requireNotBlank(policyCurrency, "policyCurrency").toUpperCase();
        this.status = status == null ? OrderStatus.PENDING_APPROVAL : status;
        this.financialStatus = financialStatus == null ? FinancialStatus.PENDING : financialStatus;
        this.subtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
        this.totalAmount = totalAmount == null ? this.subtotal : totalAmount;
        this.version = version;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        this.linesById = linesById == null ? new LinkedHashMap<>() : new LinkedHashMap<>(linesById);
        this.paymentsById = paymentsById == null ? new LinkedHashMap<>() : new LinkedHashMap<>(paymentsById);
        this.domainEvents = domainEvents == null ? new ArrayList<>() : domainEvents;
        validateInvariants();
    }

    public static Order createFromValidatedCart(
            String organizationId,

            String userId,
            String cartId,
            String checkoutCorrelationId,
            String addressId,
            String countryCode,
            long regionalPolicyVersion,
            String policyCurrency,
            List<OrderLine> lines,
            Instant now) {
        Instant created = now == null ? Instant.now() : now;
        String orderId = UUID.randomUUID().toString();
        String orderNumber = "ORD-" + created.toEpochMilli() + "-" + orderId.substring(0, 8).toUpperCase();

        Map<String, OrderLine> lineMap = new LinkedHashMap<>();
        for (OrderLine line : requiredLines(lines)) {
            OrderLine boundLine = new OrderLine(
                    line.orderLineId(),
                    orderId,
                    organizationId,
                    line.variantId(),
                    line.sku(),
                    line.qty(),
                    line.unitPrice(),
                    line.currency(),
                    line.reservationId(),
                    line.reservationConfirmed(),
                    null,
                    line.createdAt(),
                    line.updatedAt());
            lineMap.put(boundLine.orderLineId(), boundLine);
        }

        BigDecimal subtotal = calculateSubtotal(lineMap.values());
        Order order = new Order(
                orderId,
                orderNumber,
                organizationId,
                userId,
                cartId,
                checkoutCorrelationId,
                addressId,
                countryCode,
                regionalPolicyVersion,
                policyCurrency,
                OrderStatus.PENDING_APPROVAL,
                FinancialStatus.PENDING,
                subtotal,
                subtotal,
                0L,
                created,
                created,
                lineMap,
                new LinkedHashMap<>(),
                new ArrayList<>());

        order.domainEvents.add(new OrderCreatedFromValidatedCart(created, orderId, organizationId, cartId, orderNumber));
        return order;
    }

    public static Order rehydrate(
            String orderId,
            String orderNumber,
            String organizationId,

            String userId,
            String cartId,
            String checkoutCorrelationId,
            String addressId,
            String countryCode,
            long regionalPolicyVersion,
            String policyCurrency,
            OrderStatus status,
            FinancialStatus financialStatus,
            BigDecimal subtotal,
            BigDecimal totalAmount,
            long version,
            Instant createdAt,
            Instant updatedAt,
            List<OrderLine> lines,
            List<ManualPayment> payments) {
        Map<String, OrderLine> lineMap = new LinkedHashMap<>();
        if (lines != null) {
            for (OrderLine line : lines) {
                lineMap.put(line.orderLineId(), line);
            }
        }

        Map<String, ManualPayment> paymentMap = new LinkedHashMap<>();
        if (payments != null) {
            for (ManualPayment payment : payments) {
                paymentMap.put(payment.paymentRecordId(), payment);
            }
        }

        return new Order(
                orderId,
                orderNumber,
                organizationId,
                userId,
                cartId,
                checkoutCorrelationId,
                addressId,
                countryCode,
                regionalPolicyVersion,
                policyCurrency,
                status,
                financialStatus,
                subtotal,
                totalAmount,
                version,
                createdAt,
                updatedAt,
                lineMap,
                paymentMap,
                new ArrayList<>());
    }

    public Order adjustBeforeClose(List<OrderLine> adjustedLines, Instant now) {
        if (!status.allowsAdjustmentBeforeClose()) {
            throw new InvalidOrderTransitionException("Order does not allow adjustments in current status");
        }
        Map<String, OrderLine> lineMap = new LinkedHashMap<>();
        for (OrderLine line : requiredLines(adjustedLines)) {
            lineMap.put(line.orderLineId(), line);
        }
        Instant changedAt = now == null ? Instant.now() : now;
        Order next = new Order(
                orderId,
                orderNumber,
                organizationId,
                userId,
                cartId,
                checkoutCorrelationId,
                addressId,
                countryCode,
                regionalPolicyVersion,
                policyCurrency,
                status,
                financialStatus,
                calculateSubtotal(lineMap.values()),
                calculateSubtotal(lineMap.values()),
                version + 1,
                createdAt,
                changedAt,
                lineMap,
                paymentsById,
                domainEvents);
        next.domainEvents.add(new OrderAdjustedBeforeClose(changedAt, orderId));
        return next;
    }

    public Order revalidateConsistencyAfterAdjustment(Instant now) {
        ensureLinesHaveConfirmedReservations();
        Instant changedAt = now == null ? Instant.now() : now;
        Order next = new Order(
                orderId,
                orderNumber,
                organizationId,
                userId,
                cartId,
                checkoutCorrelationId,
                addressId,
                countryCode,
                regionalPolicyVersion,
                policyCurrency,
                status,
                recalculateFinancialStatus(),
                subtotal,
                totalAmount,
                version + 1,
                createdAt,
                changedAt,
                linesById,
                paymentsById,
                domainEvents);
        next.domainEvents.add(new OrderConsistencyRevalidated(changedAt, orderId));
        next.domainEvents.add(new OrderFinancialStatusUpdated(changedAt, orderId, next.financialStatus.name()));
        return next;
    }

    public Order updateOperationalStatus(OrderStatus targetStatus, String reason, Instant now) {
        if (!status.canTransitionTo(targetStatus)) {
            throw new InvalidOrderTransitionException("Invalid order status transition " + status + " -> " + targetStatus);
        }
        Instant changedAt = now == null ? Instant.now() : now;
        Order next = new Order(
                orderId,
                orderNumber,
                organizationId,
                userId,
                cartId,
                checkoutCorrelationId,
                addressId,
                countryCode,
                regionalPolicyVersion,
                policyCurrency,
                targetStatus,
                financialStatus,
                subtotal,
                totalAmount,
                version + 1,
                createdAt,
                changedAt,
                linesById,
                paymentsById,
                domainEvents);
        next.domainEvents.add(new OrderOperationalStatusUpdated(changedAt, orderId, status.name(), targetStatus.name()));
        return next;
    }

    public Order registerManualPayment(
            String paymentReference,
            BigDecimal amount,
            String method,
            String supportReference,
            Instant receivedAt,
            Instant now) {
        ensurePaymentReferenceIsUnique(paymentReference);
        ManualPayment payment = new ManualPayment(
                UUID.randomUUID().toString(),
                orderId,
                organizationId,
                paymentReference,
                amount,
                method,
                supportReference,
                ManualPaymentStatus.REGISTERED,
                receivedAt,
                now,
                now);
        Map<String, ManualPayment> nextPayments = new LinkedHashMap<>(paymentsById);
        nextPayments.put(payment.paymentRecordId(), payment);
        Instant changedAt = now == null ? Instant.now() : now;
        FinancialStatus nextFinancialStatus = recalculateFinancialStatus(nextPayments.values());

        Order next = new Order(
                orderId,
                orderNumber,
                organizationId,
                userId,
                cartId,
                checkoutCorrelationId,
                addressId,
                countryCode,
                regionalPolicyVersion,
                policyCurrency,
                status,
                nextFinancialStatus,
                subtotal,
                totalAmount,
                version + 1,
                createdAt,
                changedAt,
                linesById,
                nextPayments,
                domainEvents);
        next.domainEvents.add(new ManualPaymentRegistered(changedAt, orderId, paymentReference));
        next.domainEvents.add(new OrderFinancialStatusUpdated(changedAt, orderId, nextFinancialStatus.name()));
        return next;
    }

    public Order updateManualPaymentStatus(
            String paymentRecordId,
            ManualPaymentStatus targetStatus,
            Instant now) {
        if (paymentRecordId == null || paymentRecordId.isBlank()) {
            throw new ManualPaymentException("paymentRecordId is required");
        }
        if (targetStatus == null) {
            throw new ManualPaymentException("targetStatus is required");
        }

        Map<String, ManualPayment> nextPayments = new LinkedHashMap<>(paymentsById);
        ManualPayment current = nextPayments.get(paymentRecordId);
        if (current == null) {
            throw new ManualPaymentException("payment record not found for order");
        }

        Instant changedAt = now == null ? Instant.now() : now;
        ManualPayment updated = switch (targetStatus) {
            case REGISTERED -> current;
            case VALIDATED -> current.validate(changedAt);
            case REJECTED -> current.reject(changedAt);
        };
        nextPayments.put(updated.paymentRecordId(), updated);
        FinancialStatus nextFinancialStatus = recalculateFinancialStatus(nextPayments.values());

        Order next = new Order(
                orderId,
                orderNumber,
                organizationId,
                userId,
                cartId,
                checkoutCorrelationId,
                addressId,
                countryCode,
                regionalPolicyVersion,
                policyCurrency,
                status,
                nextFinancialStatus,
                subtotal,
                totalAmount,
                version + 1,
                createdAt,
                changedAt,
                linesById,
                nextPayments,
                domainEvents);
        next.domainEvents.add(new OrderFinancialStatusUpdated(changedAt, orderId, nextFinancialStatus.name()));
        return next;
    }

    public List<OrderLine> lines() {
        return List.copyOf(linesById.values());
    }

    public List<ManualPayment> payments() {
        return List.copyOf(paymentsById.values());
    }

    public BigDecimal paidAmount() {
        return paymentsById.values().stream()
                .filter(payment -> payment.status() != ManualPaymentStatus.REJECTED)
                .map(ManualPayment::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal pendingAmount() {
        BigDecimal pending = totalAmount.subtract(paidAmount());
        return pending.signum() < 0 ? BigDecimal.ZERO : pending;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public String orderId() {
        return orderId;
    }

    public String orderNumber() {
        return orderNumber;
    }

    public String organizationId() {
        return organizationId;
    }

    public String userId() {
        return userId;
    }

    public String cartId() {
        return cartId;
    }

    public String checkoutCorrelationId() {
        return checkoutCorrelationId;
    }

    public String addressId() {
        return addressId;
    }

    public String countryCode() {
        return countryCode;
    }

    public long regionalPolicyVersion() {
        return regionalPolicyVersion;
    }

    public String policyCurrency() {
        return policyCurrency;
    }

    public OrderStatus status() {
        return status;
    }

    public FinancialStatus financialStatus() {
        return financialStatus;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal totalAmount() {
        return totalAmount;
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

    private FinancialStatus recalculateFinancialStatus() {
        return recalculateFinancialStatus(paymentsById.values());
    }

    private FinancialStatus recalculateFinancialStatus(Iterable<ManualPayment> payments) {
        BigDecimal paid = BigDecimal.ZERO;
        for (ManualPayment payment : payments) {
            if (payment.status() == ManualPaymentStatus.REJECTED) {
                continue;
            }
            paid = paid.add(payment.amount());
        }
        if (paid.signum() == 0) {
            return FinancialStatus.PENDING;
        }
        int compare = paid.compareTo(totalAmount);
        if (compare < 0) {
            return FinancialStatus.PARTIALLY_PAID;
        }
        if (compare == 0) {
            return FinancialStatus.PAID_IN_FULL;
        }
        return FinancialStatus.OVERPAID_REVIEW;
    }

    private void ensurePaymentReferenceIsUnique(String paymentReference) {
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new ManualPaymentException("paymentReference is required");
        }
        for (ManualPayment payment : paymentsById.values()) {
            if (payment.paymentReference().equalsIgnoreCase(paymentReference.trim())) {
                throw new ManualPaymentException("payment reference already exists for order");
            }
        }
    }

    private void ensureLinesHaveConfirmedReservations() {
        if (linesById.isEmpty()) {
            throw new OrderConsistencyException("order requires at least one line");
        }
        for (OrderLine line : linesById.values()) {
            if (!line.reservationConfirmed() || line.reservationId() == null || line.reservationId().isBlank()) {
                throw new OrderConsistencyException("all order lines require confirmed reservation_id");
            }
        }
    }

    private void validateInvariants() {
        if (version < 0) {
            throw new DomainInvariantViolationException("version must be non-negative");
        }
        if (regionalPolicyVersion <= 0) {
            throw new DomainInvariantViolationException("regionalPolicyVersion must be positive");
        }
        if (subtotal.signum() < 0 || totalAmount.signum() < 0) {
            throw new DomainInvariantViolationException("totals cannot be negative");
        }
        ensureLinesHaveConfirmedReservations();
    }

    private static List<OrderLine> requiredLines(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new OrderConsistencyException("order requires at least one line");
        }
        return List.copyOf(lines);
    }

    private static BigDecimal calculateSubtotal(Iterable<OrderLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            subtotal = subtotal.add(line.lineTotal());
        }
        return subtotal;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
        return value.trim();
    }
}
