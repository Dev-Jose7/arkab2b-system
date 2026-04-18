package com.arka.order.infrastructure.adapter.in.web.mapper.command;

import com.arka.order.application.command.AdjustCartItemInput;
import com.arka.order.application.command.AdjustCartItemsCommand;
import com.arka.order.application.command.AdjustOrderBeforeCloseCommand;
import com.arka.order.application.command.AdjustOrderLineInput;
import com.arka.order.application.command.CancelOrderCommand;
import com.arka.order.application.command.CreateCartCommand;
import com.arka.order.application.command.CreateOrderFromCartCommand;
import com.arka.order.application.command.HandleReservationExpiredCommand;
import com.arka.order.application.command.RegisterManualPaymentCommand;
import com.arka.order.application.command.RevalidateOrderConsistencyAfterAdjustmentCommand;
import com.arka.order.application.command.UpdateOrderOperationalStatusCommand;
import com.arka.order.application.command.ValidateCheckoutAvailabilityCommand;
import com.arka.order.application.command.ValidateManualPaymentCommand;
import com.arka.order.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.order.infrastructure.adapter.in.web.request.AdjustCartItemRequest;
import com.arka.order.infrastructure.adapter.in.web.request.AdjustCartItemsRequest;
import com.arka.order.infrastructure.adapter.in.web.request.AdjustOrderBeforeCloseRequest;
import com.arka.order.infrastructure.adapter.in.web.request.AdjustOrderLineRequest;
import com.arka.order.infrastructure.adapter.in.web.request.CancelOrderRequest;
import com.arka.order.infrastructure.adapter.in.web.request.CreateCartRequest;
import com.arka.order.infrastructure.adapter.in.web.request.CreateOrderRequest;
import com.arka.order.infrastructure.adapter.in.web.request.RegisterManualPaymentRequest;
import com.arka.order.infrastructure.adapter.in.web.request.ReservationExpiredEventRequest;
import com.arka.order.infrastructure.adapter.in.web.request.RevalidateOrderRequest;
import com.arka.order.infrastructure.adapter.in.web.request.UpdateOrderStatusRequest;
import com.arka.order.infrastructure.adapter.in.web.request.ValidateCheckoutRequest;
import com.arka.order.infrastructure.adapter.in.web.request.ValidateManualPaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderCommandMapper {

    public CreateCartCommand toCommand(
            CreateCartRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        String userId = request == null || request.userId() == null || request.userId().isBlank()
                ? principal.userId()
                : request.userId().trim();
        return new CreateCartCommand(
                principal.tenantId(),
                principal.organizationId(),
                userId,
                principal.userId(),
                idempotencyKey);
    }

    public AdjustCartItemsCommand toCommand(
            String cartId,
            AdjustCartItemsRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new AdjustCartItemsCommand(
                principal.tenantId(),
                principal.organizationId(),
                principal.userId(),
                cartId,
                request.items().stream().map(this::toInput).toList(),
                principal.userId(),
                idempotencyKey);
    }

    public ValidateCheckoutAvailabilityCommand toCommand(
            String cartId,
            ValidateCheckoutRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new ValidateCheckoutAvailabilityCommand(
                principal.tenantId(),
                principal.organizationId(),
                principal.userId(),
                cartId,
                request.checkoutCorrelationId(),
                request.addressId(),
                request.countryCode(),
                principal.userId(),
                idempotencyKey);
    }

    public CreateOrderFromCartCommand toCommand(
            CreateOrderRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        String userId = request.userId() == null || request.userId().isBlank()
                ? principal.userId()
                : request.userId().trim();
        return new CreateOrderFromCartCommand(
                principal.tenantId(),
                principal.organizationId(),
                userId,
                request.cartId(),
                request.checkoutCorrelationId(),
                principal.userId(),
                idempotencyKey);
    }

    public AdjustOrderBeforeCloseCommand toCommand(
            String orderId,
            AdjustOrderBeforeCloseRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new AdjustOrderBeforeCloseCommand(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                request.lines().stream().map(this::toInput).toList(),
                request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public RevalidateOrderConsistencyAfterAdjustmentCommand toCommand(
            String orderId,
            RevalidateOrderRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new RevalidateOrderConsistencyAfterAdjustmentCommand(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                request == null ? null : request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public CancelOrderCommand toCommand(
            String orderId,
            CancelOrderRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new CancelOrderCommand(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                request == null ? null : request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public UpdateOrderOperationalStatusCommand toCommand(
            String orderId,
            UpdateOrderStatusRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new UpdateOrderOperationalStatusCommand(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                request.targetStatus(),
                request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public RegisterManualPaymentCommand toCommand(
            String orderId,
            RegisterManualPaymentRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new RegisterManualPaymentCommand(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                request.paymentReference(),
                request.amount(),
                request.method(),
                request.supportReference(),
                request.receivedAt(),
                principal.userId(),
                idempotencyKey);
    }

    public ValidateManualPaymentCommand toCommand(
            String orderId,
            String paymentRecordId,
            ValidateManualPaymentRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new ValidateManualPaymentCommand(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                paymentRecordId,
                request.targetStatus(),
                request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public HandleReservationExpiredCommand toCommand(
            String cartId,
            ReservationExpiredEventRequest request,
            IamSecurityPrincipal principal) {
        return new HandleReservationExpiredCommand(
                principal.tenantId(),
                request.organizationId(),
                cartId,
                request.reservationId(),
                request.eventId(),
                principal.userId());
    }

    private AdjustCartItemInput toInput(AdjustCartItemRequest request) {
        return new AdjustCartItemInput(
                request.cartItemId(),
                request.operation(),
                request.variantId(),
                request.sku(),
                request.qty(),
                request.unitPrice(),
                request.currency(),
                request.reservationId(),
                request.reservationConfirmed());
    }

    private AdjustOrderLineInput toInput(AdjustOrderLineRequest request) {
        return new AdjustOrderLineInput(
                request.orderLineId(),
                request.variantId(),
                request.sku(),
                request.qty(),
                request.unitPrice(),
                request.currency(),
                request.reservationId(),
                request.reservationConfirmed());
    }
}
