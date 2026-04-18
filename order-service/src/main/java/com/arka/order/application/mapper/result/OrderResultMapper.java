package com.arka.order.application.mapper.result;

import com.arka.order.application.result.CartItemResult;
import com.arka.order.application.result.CartResult;
import com.arka.order.application.result.CheckoutAttemptResult;
import com.arka.order.application.result.ManualPaymentResult;
import com.arka.order.application.result.OrderFinancialStatusResult;
import com.arka.order.application.result.OrderLineResult;
import com.arka.order.application.result.OrderResult;
import com.arka.order.application.result.OrderStatusHistoryResult;
import com.arka.order.application.result.OrderSummaryResult;
import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.domain.cart.entity.CartItem;
import com.arka.order.domain.cart.entity.CheckoutAttempt;
import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.domain.order.entity.ManualPayment;
import com.arka.order.domain.order.entity.OrderLine;
import com.arka.order.domain.order.entity.OrderStatusHistory;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderResultMapper {

    public CartResult toResult(Cart cart) {
        return new CartResult(
                cart.cartId(),
                cart.organizationId(),
                cart.userId(),
                cart.status().name(),
                cart.subtotal(),
                cart.version(),
                cart.createdAt(),
                cart.updatedAt(),
                cart.items().stream().map(this::toResult).toList());
    }

    public CartItemResult toResult(CartItem item) {
        return new CartItemResult(
                item.cartItemId(),
                item.variantId(),
                item.sku(),
                item.qty(),
                item.unitPrice(),
                item.currency(),
                item.reservationId(),
                item.reservationConfirmed(),
                item.lineSubtotal(),
                item.createdAt(),
                item.updatedAt());
    }

    public CheckoutAttemptResult toResult(CheckoutAttempt attempt) {
        return new CheckoutAttemptResult(
                attempt.checkoutAttemptId(),
                attempt.checkoutCorrelationId(),
                attempt.organizationId(),
                attempt.userId(),
                attempt.cartId(),
                attempt.validationStatus().name(),
                attempt.addressId(),
                attempt.countryCode(),
                attempt.regionalPolicyVersion(),
                attempt.policyCurrency(),
                attempt.rejectionReasons(),
                attempt.createdAt(),
                attempt.updatedAt());
    }

    public OrderResult toResult(Order order) {
        BigDecimal paidAmount = order.paidAmount();
        BigDecimal pendingAmount = order.totalAmount().subtract(paidAmount);
        if (pendingAmount.signum() < 0) {
            pendingAmount = BigDecimal.ZERO;
        }
        return new OrderResult(
                order.orderId(),
                order.orderNumber(),
                order.organizationId(),
                order.userId(),
                order.cartId(),
                order.checkoutCorrelationId(),
                order.addressId(),
                order.countryCode(),
                order.regionalPolicyVersion(),
                order.policyCurrency(),
                order.status().name(),
                order.financialStatus().name(),
                order.subtotal(),
                order.totalAmount(),
                paidAmount,
                pendingAmount,
                order.version(),
                order.createdAt(),
                order.updatedAt(),
                order.lines().stream().map(this::toResult).toList(),
                order.payments().stream().map(this::toResult).toList());
    }

    public OrderSummaryResult toSummaryResult(Order order) {
        return new OrderSummaryResult(
                order.orderId(),
                order.orderNumber(),
                order.status().name(),
                order.financialStatus().name(),
                order.totalAmount(),
                order.paidAmount(),
                order.createdAt(),
                order.updatedAt());
    }

    public OrderLineResult toResult(OrderLine line) {
        return new OrderLineResult(
                line.orderLineId(),
                line.variantId(),
                line.sku(),
                line.qty(),
                line.unitPrice(),
                line.currency(),
                line.reservationId(),
                line.reservationConfirmed(),
                line.lineTotal());
    }

    public ManualPaymentResult toResult(ManualPayment payment) {
        return new ManualPaymentResult(
                payment.paymentRecordId(),
                payment.paymentReference(),
                payment.amount(),
                payment.method(),
                payment.supportReference(),
                payment.status().name(),
                payment.receivedAt(),
                payment.createdAt(),
                payment.updatedAt());
    }

    public OrderStatusHistoryResult toResult(OrderStatusHistory history) {
        return new OrderStatusHistoryResult(
                history.statusHistoryId(),
                history.actorUserId(),
                history.fromStatus() == null ? null : history.fromStatus().name(),
                history.toStatus().name(),
                history.reason(),
                history.occurredAt());
    }

    public OrderFinancialStatusResult toFinancialStatusResult(Order order) {
        BigDecimal paid = order.paidAmount();
        BigDecimal pending = order.totalAmount().subtract(paid);
        if (pending.signum() < 0) {
            pending = BigDecimal.ZERO;
        }
        return new OrderFinancialStatusResult(
                order.orderId(),
                order.financialStatus().name(),
                order.totalAmount(),
                paid,
                pending);
    }
}
