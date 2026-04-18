package com.arka.order.infrastructure.adapter.out.persistence.mapper;

import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.domain.order.entity.ManualPayment;
import com.arka.order.domain.order.entity.OrderLine;
import com.arka.order.domain.order.entity.OrderStatusHistory;
import com.arka.order.domain.order.enumtype.FinancialStatus;
import com.arka.order.domain.order.enumtype.ManualPaymentStatus;
import com.arka.order.domain.order.enumtype.OrderStatus;
import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderLineEntity;
import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderStatusHistoryEntity;
import com.arka.order.infrastructure.adapter.out.persistence.entity.PaymentRecordEntity;
import com.arka.order.infrastructure.adapter.out.persistence.entity.PurchaseOrderEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderPersistenceMapper {

    public PurchaseOrderEntity toEntity(Order order) {
        return new PurchaseOrderEntity(
                order.orderId(),
                order.orderNumber(),
                order.tenantId(),
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
                order.version(),
                order.createdAt(),
                order.updatedAt());
    }

    public List<OrderLineEntity> toOrderLineEntities(Order order) {
        return order.lines().stream().map(this::toOrderLineEntity).toList();
    }

    public List<PaymentRecordEntity> toPaymentRecordEntities(Order order) {
        return order.payments().stream().map(this::toPaymentRecordEntity).toList();
    }

    public OrderLineEntity toOrderLineEntity(OrderLine line) {
        return new OrderLineEntity(
                line.orderLineId(),
                line.orderId(),
                line.tenantId(),
                line.organizationId(),
                line.variantId(),
                line.sku(),
                line.qty(),
                line.unitPrice(),
                line.currency(),
                line.reservationId(),
                line.reservationConfirmed(),
                line.lineTotal(),
                line.createdAt(),
                line.updatedAt());
    }

    public PaymentRecordEntity toPaymentRecordEntity(ManualPayment payment) {
        return new PaymentRecordEntity(
                payment.paymentRecordId(),
                payment.orderId(),
                payment.tenantId(),
                payment.organizationId(),
                payment.paymentReference(),
                payment.amount(),
                payment.method(),
                payment.supportReference(),
                payment.status().name(),
                payment.receivedAt(),
                payment.createdAt(),
                payment.updatedAt());
    }

    public Order toDomain(PurchaseOrderEntity order, List<OrderLineEntity> lines, List<PaymentRecordEntity> payments) {
        return Order.rehydrate(
                order.orderId(),
                order.orderNumber(),
                order.tenantId(),
                order.organizationId(),
                order.userId(),
                order.cartId(),
                order.checkoutCorrelationId(),
                order.addressId(),
                order.countryCode(),
                order.regionalPolicyVersion(),
                order.policyCurrency(),
                OrderStatus.valueOf(order.status()),
                FinancialStatus.valueOf(order.paymentStatus()),
                order.subtotal(),
                order.totalAmount(),
                order.version(),
                order.createdAt(),
                order.updatedAt(),
                lines == null ? List.of() : lines.stream().map(this::toDomain).toList(),
                payments == null ? List.of() : payments.stream().map(this::toDomain).toList());
    }

    public OrderLine toDomain(OrderLineEntity line) {
        return new OrderLine(
                line.orderLineId(),
                line.orderId(),
                line.tenantId(),
                line.organizationId(),
                line.variantId(),
                line.sku(),
                line.qty(),
                line.unitPrice(),
                line.currency(),
                line.reservationId(),
                line.reservationConfirmed(),
                line.lineTotal(),
                line.createdAt(),
                line.updatedAt());
    }

    public ManualPayment toDomain(PaymentRecordEntity payment) {
        return new ManualPayment(
                payment.paymentRecordId(),
                payment.orderId(),
                payment.tenantId(),
                payment.organizationId(),
                payment.paymentReference(),
                payment.amount(),
                payment.method(),
                payment.supportReference(),
                ManualPaymentStatus.valueOf(payment.status()),
                payment.receivedAt(),
                payment.createdAt(),
                payment.updatedAt());
    }

    public OrderStatusHistoryEntity toEntity(OrderStatusHistory history) {
        return new OrderStatusHistoryEntity(
                history.statusHistoryId(),
                history.orderId(),
                history.tenantId(),
                history.actorUserId(),
                history.fromStatus() == null ? null : history.fromStatus().name(),
                history.toStatus().name(),
                history.reason(),
                history.occurredAt());
    }

    public OrderStatusHistory toDomain(OrderStatusHistoryEntity history) {
        return new OrderStatusHistory(
                history.statusHistoryId(),
                history.orderId(),
                history.tenantId(),
                history.actorUserId(),
                history.fromStatus() == null || history.fromStatus().isBlank()
                        ? null
                        : OrderStatus.valueOf(history.fromStatus()),
                OrderStatus.valueOf(history.toStatus()),
                history.reason(),
                history.occurredAt());
    }
}
