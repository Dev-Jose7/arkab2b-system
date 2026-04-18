package com.arka.order.infrastructure.adapter.in.web.mapper.response;

import com.arka.order.application.result.CartItemResult;
import com.arka.order.application.result.CartResult;
import com.arka.order.application.result.CheckoutAttemptResult;
import com.arka.order.application.result.ManualPaymentResult;
import com.arka.order.application.result.OrderAmountsResult;
import com.arka.order.application.result.OrderAuditEntryResult;
import com.arka.order.application.result.OrderAuditResult;
import com.arka.order.application.result.OrderFinancialStatusResult;
import com.arka.order.application.result.OrderLineResult;
import com.arka.order.application.result.OrderResult;
import com.arka.order.application.result.OrderStatusHistoryResult;
import com.arka.order.application.result.OrderSummaryResult;
import com.arka.order.infrastructure.adapter.in.web.response.CartItemResponse;
import com.arka.order.infrastructure.adapter.in.web.response.CartResponse;
import com.arka.order.infrastructure.adapter.in.web.response.CheckoutAttemptResponse;
import com.arka.order.infrastructure.adapter.in.web.response.ManualPaymentResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderAmountsResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderAuditEntryResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderAuditResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderFinancialStatusResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderLineResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderStatusHistoryResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseMapper {

    public CartResponse toResponse(CartResult result) {
        return new CartResponse(
                result.cartId(),
                result.tenantId(),
                result.organizationId(),
                result.userId(),
                result.status(),
                result.subtotal(),
                result.version(),
                result.createdAt(),
                result.updatedAt(),
                result.items().stream().map(this::toResponse).toList());
    }

    public CheckoutAttemptResponse toResponse(CheckoutAttemptResult result) {
        return new CheckoutAttemptResponse(
                result.checkoutAttemptId(),
                result.checkoutCorrelationId(),
                result.tenantId(),
                result.organizationId(),
                result.userId(),
                result.cartId(),
                result.validationStatus(),
                result.addressId(),
                result.countryCode(),
                result.regionalPolicyVersion(),
                result.policyCurrency(),
                result.rejectionReasons(),
                result.createdAt(),
                result.updatedAt());
    }

    public OrderResponse toResponse(OrderResult result) {
        return new OrderResponse(
                result.orderId(),
                result.orderNumber(),
                result.tenantId(),
                result.organizationId(),
                result.userId(),
                result.cartId(),
                result.checkoutCorrelationId(),
                result.addressId(),
                result.countryCode(),
                result.regionalPolicyVersion(),
                result.policyCurrency(),
                result.status(),
                result.financialStatus(),
                result.subtotal(),
                result.totalAmount(),
                result.paidAmount(),
                result.pendingAmount(),
                result.version(),
                result.createdAt(),
                result.updatedAt(),
                result.lines().stream().map(this::toResponse).toList(),
                result.payments().stream().map(this::toResponse).toList());
    }

    public OrderSummaryResponse toResponse(OrderSummaryResult result) {
        return new OrderSummaryResponse(
                result.orderId(),
                result.orderNumber(),
                result.status(),
                result.financialStatus(),
                result.totalAmount(),
                result.paidAmount(),
                result.createdAt(),
                result.updatedAt());
    }

    public OrderStatusHistoryResponse toResponse(OrderStatusHistoryResult result) {
        return new OrderStatusHistoryResponse(
                result.statusHistoryId(),
                result.actorUserId(),
                result.fromStatus(),
                result.toStatus(),
                result.reason(),
                result.occurredAt());
    }

    public ManualPaymentResponse toResponse(ManualPaymentResult result) {
        return new ManualPaymentResponse(
                result.paymentRecordId(),
                result.paymentReference(),
                result.amount(),
                result.method(),
                result.supportReference(),
                result.status(),
                result.receivedAt(),
                result.createdAt(),
                result.updatedAt());
    }

    public OrderFinancialStatusResponse toResponse(OrderFinancialStatusResult result) {
        return new OrderFinancialStatusResponse(
                result.orderId(),
                result.financialStatus(),
                result.totalAmount(),
                result.paidAmount(),
                result.pendingAmount());
    }

    public OrderAuditResponse toResponse(OrderAuditResult result) {
        return new OrderAuditResponse(
                result.tenantId(),
                result.organizationId(),
                result.orderId(),
                result.entries().stream().map(this::toResponse).toList());
    }

    public OrderAmountsResponse toResponse(OrderAmountsResult result) {
        return new OrderAmountsResponse(
                result.orderId(),
                result.subtotal(),
                result.totalAmount(),
                result.paidAmount(),
                result.pendingAmount());
    }

    private CartItemResponse toResponse(CartItemResult result) {
        return new CartItemResponse(
                result.cartItemId(),
                result.variantId(),
                result.sku(),
                result.qty(),
                result.unitPrice(),
                result.currency(),
                result.reservationId(),
                result.reservationConfirmed(),
                result.lineSubtotal(),
                result.createdAt(),
                result.updatedAt());
    }

    private OrderLineResponse toResponse(OrderLineResult result) {
        return new OrderLineResponse(
                result.orderLineId(),
                result.variantId(),
                result.sku(),
                result.qty(),
                result.unitPrice(),
                result.currency(),
                result.reservationId(),
                result.reservationConfirmed(),
                result.lineTotal());
    }

    private OrderAuditEntryResponse toResponse(OrderAuditEntryResult result) {
        return new OrderAuditEntryResponse(
                result.auditId(),
                result.tenantId(),
                result.organizationId(),
                result.actorUserId(),
                result.actionType(),
                result.targetType(),
                result.targetId(),
                result.outcome(),
                result.payload(),
                result.createdAt());
    }
}
