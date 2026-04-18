package com.arka.order.infrastructure.adapter.in.web.controller;

import com.arka.order.application.port.in.AdjustCartItemsCommandUseCase;
import com.arka.order.application.port.in.AdjustOrderBeforeCloseCommandUseCase;
import com.arka.order.application.port.in.CalculateOrderAmountsQueryUseCase;
import com.arka.order.application.port.in.CancelOrderCommandUseCase;
import com.arka.order.application.port.in.CreateCartCommandUseCase;
import com.arka.order.application.port.in.CreateOrderFromCartCommandUseCase;
import com.arka.order.application.port.in.GetActiveCartQueryUseCase;
import com.arka.order.application.port.in.GetCartQueryUseCase;
import com.arka.order.application.port.in.GetCheckoutAttemptByCorrelationQueryUseCase;
import com.arka.order.application.port.in.GetOrderAuditQueryUseCase;
import com.arka.order.application.port.in.GetOrderFinancialStatusQueryUseCase;
import com.arka.order.application.port.in.GetOrderQueryUseCase;
import com.arka.order.application.port.in.GetOrderTimelineQueryUseCase;
import com.arka.order.application.port.in.HandleReservationExpiredCommandUseCase;
import com.arka.order.application.port.in.ListOrderPaymentsQueryUseCase;
import com.arka.order.application.port.in.ListOrdersQueryUseCase;
import com.arka.order.application.port.in.RegisterManualPaymentCommandUseCase;
import com.arka.order.application.port.in.RevalidateOrderConsistencyAfterAdjustmentCommandUseCase;
import com.arka.order.application.port.in.UpdateOrderOperationalStatusCommandUseCase;
import com.arka.order.application.port.in.ValidateCheckoutAvailabilityCommandUseCase;
import com.arka.order.application.port.in.ValidateManualPaymentCommandUseCase;
import com.arka.order.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.order.infrastructure.adapter.in.web.mapper.command.OrderCommandMapper;
import com.arka.order.infrastructure.adapter.in.web.mapper.query.OrderQueryMapper;
import com.arka.order.infrastructure.adapter.in.web.mapper.response.OrderResponseMapper;
import com.arka.order.infrastructure.adapter.in.web.request.AdjustCartItemsRequest;
import com.arka.order.infrastructure.adapter.in.web.request.AdjustOrderBeforeCloseRequest;
import com.arka.order.infrastructure.adapter.in.web.request.CancelOrderRequest;
import com.arka.order.infrastructure.adapter.in.web.request.CreateCartRequest;
import com.arka.order.infrastructure.adapter.in.web.request.CreateOrderRequest;
import com.arka.order.infrastructure.adapter.in.web.request.RegisterManualPaymentRequest;
import com.arka.order.infrastructure.adapter.in.web.request.ReservationExpiredEventRequest;
import com.arka.order.infrastructure.adapter.in.web.request.RevalidateOrderRequest;
import com.arka.order.infrastructure.adapter.in.web.request.UpdateOrderStatusRequest;
import com.arka.order.infrastructure.adapter.in.web.request.ValidateCheckoutRequest;
import com.arka.order.infrastructure.adapter.in.web.request.ValidateManualPaymentRequest;
import com.arka.order.infrastructure.adapter.in.web.response.CartResponse;
import com.arka.order.infrastructure.adapter.in.web.response.CheckoutAttemptResponse;
import com.arka.order.infrastructure.adapter.in.web.response.ManualPaymentResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderAmountsResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderAuditResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderFinancialStatusResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderStatusHistoryResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrderSummaryResponse;
import com.arka.order.infrastructure.adapter.in.web.response.OrganizationContextResponse;
import com.arka.order.infrastructure.adapter.out.persistence.repository.CartR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.PurchaseOrderR2dbcRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderCommandMapper commandMapper;
    private final OrderQueryMapper queryMapper;
    private final OrderResponseMapper responseMapper;
    private final CreateCartCommandUseCase createCartCommandUseCase;
    private final AdjustCartItemsCommandUseCase adjustCartItemsCommandUseCase;
    private final ValidateCheckoutAvailabilityCommandUseCase validateCheckoutAvailabilityCommandUseCase;
    private final CreateOrderFromCartCommandUseCase createOrderFromCartCommandUseCase;
    private final AdjustOrderBeforeCloseCommandUseCase adjustOrderBeforeCloseCommandUseCase;
    private final RevalidateOrderConsistencyAfterAdjustmentCommandUseCase revalidateOrderConsistencyAfterAdjustmentCommandUseCase;
    private final CancelOrderCommandUseCase cancelOrderCommandUseCase;
    private final UpdateOrderOperationalStatusCommandUseCase updateOrderOperationalStatusCommandUseCase;
    private final RegisterManualPaymentCommandUseCase registerManualPaymentCommandUseCase;
    private final ValidateManualPaymentCommandUseCase validateManualPaymentCommandUseCase;
    private final HandleReservationExpiredCommandUseCase handleReservationExpiredCommandUseCase;
    private final GetActiveCartQueryUseCase getActiveCartQueryUseCase;
    private final GetCartQueryUseCase getCartQueryUseCase;
    private final GetCheckoutAttemptByCorrelationQueryUseCase getCheckoutAttemptByCorrelationQueryUseCase;
    private final GetOrderQueryUseCase getOrderQueryUseCase;
    private final ListOrdersQueryUseCase listOrdersQueryUseCase;
    private final GetOrderTimelineQueryUseCase getOrderTimelineQueryUseCase;
    private final GetOrderFinancialStatusQueryUseCase getOrderFinancialStatusQueryUseCase;
    private final ListOrderPaymentsQueryUseCase listOrderPaymentsQueryUseCase;
    private final GetOrderAuditQueryUseCase getOrderAuditQueryUseCase;
    private final CalculateOrderAmountsQueryUseCase calculateOrderAmountsQueryUseCase;
    private final CartR2dbcRepository cartR2dbcRepository;
    private final PurchaseOrderR2dbcRepository purchaseOrderR2dbcRepository;

    public OrderController(
            OrderCommandMapper commandMapper,
            OrderQueryMapper queryMapper,
            OrderResponseMapper responseMapper,
            CreateCartCommandUseCase createCartCommandUseCase,
            AdjustCartItemsCommandUseCase adjustCartItemsCommandUseCase,
            ValidateCheckoutAvailabilityCommandUseCase validateCheckoutAvailabilityCommandUseCase,
            CreateOrderFromCartCommandUseCase createOrderFromCartCommandUseCase,
            AdjustOrderBeforeCloseCommandUseCase adjustOrderBeforeCloseCommandUseCase,
            RevalidateOrderConsistencyAfterAdjustmentCommandUseCase revalidateOrderConsistencyAfterAdjustmentCommandUseCase,
            CancelOrderCommandUseCase cancelOrderCommandUseCase,
            UpdateOrderOperationalStatusCommandUseCase updateOrderOperationalStatusCommandUseCase,
            RegisterManualPaymentCommandUseCase registerManualPaymentCommandUseCase,
            ValidateManualPaymentCommandUseCase validateManualPaymentCommandUseCase,
            HandleReservationExpiredCommandUseCase handleReservationExpiredCommandUseCase,
            GetActiveCartQueryUseCase getActiveCartQueryUseCase,
            GetCartQueryUseCase getCartQueryUseCase,
            GetCheckoutAttemptByCorrelationQueryUseCase getCheckoutAttemptByCorrelationQueryUseCase,
            GetOrderQueryUseCase getOrderQueryUseCase,
            ListOrdersQueryUseCase listOrdersQueryUseCase,
            GetOrderTimelineQueryUseCase getOrderTimelineQueryUseCase,
            GetOrderFinancialStatusQueryUseCase getOrderFinancialStatusQueryUseCase,
            ListOrderPaymentsQueryUseCase listOrderPaymentsQueryUseCase,
            GetOrderAuditQueryUseCase getOrderAuditQueryUseCase,
            CalculateOrderAmountsQueryUseCase calculateOrderAmountsQueryUseCase,
            CartR2dbcRepository cartR2dbcRepository,
            PurchaseOrderR2dbcRepository purchaseOrderR2dbcRepository) {
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.responseMapper = responseMapper;
        this.createCartCommandUseCase = createCartCommandUseCase;
        this.adjustCartItemsCommandUseCase = adjustCartItemsCommandUseCase;
        this.validateCheckoutAvailabilityCommandUseCase = validateCheckoutAvailabilityCommandUseCase;
        this.createOrderFromCartCommandUseCase = createOrderFromCartCommandUseCase;
        this.adjustOrderBeforeCloseCommandUseCase = adjustOrderBeforeCloseCommandUseCase;
        this.revalidateOrderConsistencyAfterAdjustmentCommandUseCase = revalidateOrderConsistencyAfterAdjustmentCommandUseCase;
        this.cancelOrderCommandUseCase = cancelOrderCommandUseCase;
        this.updateOrderOperationalStatusCommandUseCase = updateOrderOperationalStatusCommandUseCase;
        this.registerManualPaymentCommandUseCase = registerManualPaymentCommandUseCase;
        this.validateManualPaymentCommandUseCase = validateManualPaymentCommandUseCase;
        this.handleReservationExpiredCommandUseCase = handleReservationExpiredCommandUseCase;
        this.getActiveCartQueryUseCase = getActiveCartQueryUseCase;
        this.getCartQueryUseCase = getCartQueryUseCase;
        this.getCheckoutAttemptByCorrelationQueryUseCase = getCheckoutAttemptByCorrelationQueryUseCase;
        this.getOrderQueryUseCase = getOrderQueryUseCase;
        this.listOrdersQueryUseCase = listOrdersQueryUseCase;
        this.getOrderTimelineQueryUseCase = getOrderTimelineQueryUseCase;
        this.getOrderFinancialStatusQueryUseCase = getOrderFinancialStatusQueryUseCase;
        this.listOrderPaymentsQueryUseCase = listOrderPaymentsQueryUseCase;
        this.getOrderAuditQueryUseCase = getOrderAuditQueryUseCase;
        this.calculateOrderAmountsQueryUseCase = calculateOrderAmountsQueryUseCase;
        this.cartR2dbcRepository = cartR2dbcRepository;
        this.purchaseOrderR2dbcRepository = purchaseOrderR2dbcRepository;
    }

    @PreAuthorize("hasAnyAuthority('order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/carts")
    public Mono<CartResponse> createCart(
            @RequestBody(required = false) CreateCartRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        CreateCartRequest payload = request == null ? new CreateCartRequest(null) : request;
        return createCartCommandUseCase
                .handle(commandMapper.toCommand(payload, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PutMapping("/carts/{cartId}/items")
    public Mono<CartResponse> adjustCartItems(
            @PathVariable String cartId,
            @Valid @RequestBody AdjustCartItemsRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return adjustCartItemsCommandUseCase
                .handle(commandMapper.toCommand(cartId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/carts/{cartId}/checkout-validation")
    public Mono<CheckoutAttemptResponse> validateCheckout(
            @PathVariable String cartId,
            @Valid @RequestBody ValidateCheckoutRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return validateCheckoutAvailabilityCommandUseCase
                .handle(commandMapper.toCommand(cartId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/orders")
    public Mono<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return createOrderFromCartCommandUseCase
                .handle(commandMapper.toCommand(request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/orders/{orderId}/adjustments")
    public Mono<OrderResponse> adjustOrderBeforeClose(
            @PathVariable String orderId,
            @Valid @RequestBody AdjustOrderBeforeCloseRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return adjustOrderBeforeCloseCommandUseCase
                .handle(commandMapper.toCommand(orderId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/orders/{orderId}/revalidate")
    public Mono<OrderResponse> revalidateOrderConsistency(
            @PathVariable String orderId,
            @RequestBody(required = false) RevalidateOrderRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        RevalidateOrderRequest payload = request == null ? new RevalidateOrderRequest(null) : request;
        return revalidateOrderConsistencyAfterAdjustmentCommandUseCase
                .handle(commandMapper.toCommand(orderId, payload, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/orders/{orderId}/cancel")
    public Mono<OrderResponse> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) CancelOrderRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        CancelOrderRequest payload = request == null ? new CancelOrderRequest(null) : request;
        return cancelOrderCommandUseCase
                .handle(commandMapper.toCommand(orderId, payload, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.admin', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/orders/{orderId}/status")
    public Mono<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateOrderOperationalStatusCommandUseCase
                .handle(commandMapper.toCommand(orderId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.admin', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/orders/{orderId}/payments/manual")
    public Mono<OrderResponse> registerManualPayment(
            @PathVariable String orderId,
            @Valid @RequestBody RegisterManualPaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return registerManualPaymentCommandUseCase
                .handle(commandMapper.toCommand(orderId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.admin', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @PostMapping("/orders/{orderId}/payments/manual/{paymentRecordId}/status")
    public Mono<OrderResponse> validateManualPayment(
            @PathVariable String orderId,
            @PathVariable String paymentRecordId,
            @Valid @RequestBody ValidateManualPaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return validateManualPaymentCommandUseCase
                .handle(commandMapper.toCommand(orderId, paymentRecordId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.admin', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN', 'ROLE_TRUSTED_SERVICE')")
    @PostMapping("/internal/carts/{cartId}/reservation-expired")
    public Mono<CartResponse> handleReservationExpired(
            @PathVariable String cartId,
            @Valid @RequestBody ReservationExpiredEventRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return handleReservationExpiredCommandUseCase
                .handle(commandMapper.toCommand(cartId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasRole('TRUSTED_SERVICE') and hasAuthority('order.read')")
    @GetMapping({"/internal/orders/{orderId}/organization-context", "/internal/orders/{orderId}/organization"})
    public Mono<OrganizationContextResponse> resolveOrganizationContextByOrder(@PathVariable String orderId) {
        return purchaseOrderR2dbcRepository
                .findContextByOrderId(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found")))
                .map(order -> new OrganizationContextResponse(order.organizationId(), order.userId()));
    }

    @PreAuthorize("hasRole('TRUSTED_SERVICE') and hasAuthority('order.read')")
    @GetMapping({"/internal/carts/{cartId}/organization-context", "/internal/carts/{cartId}/organization"})
    public Mono<OrganizationContextResponse> resolveOrganizationContextByCart(@PathVariable String cartId) {
        return cartR2dbcRepository
                .findContextByCartId(cartId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "cart not found")))
                .map(cart -> new OrganizationContextResponse(cart.organizationId(), cart.userId()));
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/carts/active")
    public Mono<CartResponse> getActiveCart(
            @RequestParam(required = false) String userId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getActiveCartQueryUseCase
                .handle(queryMapper.toQuery(principal, userId))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/carts/{cartId}")
    public Mono<CartResponse> getCart(
            @PathVariable String cartId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getCartQueryUseCase
                .handle(queryMapper.toCartQuery(cartId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/carts/checkout-attempts/{checkoutCorrelationId}")
    public Mono<CheckoutAttemptResponse> getCheckoutAttempt(
            @PathVariable String checkoutCorrelationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getCheckoutAttemptByCorrelationQueryUseCase
                .handle(queryMapper.toCheckoutAttemptQuery(checkoutCorrelationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/orders/{orderId}")
    public Mono<OrderResponse> getOrder(
            @PathVariable String orderId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getOrderQueryUseCase
                .handle(queryMapper.toOrderQuery(orderId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/orders")
    public Flux<OrderSummaryResponse> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) @Min(1) @Max(500) Integer limit,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listOrdersQueryUseCase
                .handle(queryMapper.toListOrdersQuery(principal, status, createdFrom, createdTo, limit))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/orders/{orderId}/history")
    public Flux<OrderStatusHistoryResponse> getOrderHistory(
            @PathVariable String orderId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getOrderTimelineQueryUseCase
                .handle(queryMapper.toTimelineQuery(orderId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/orders/{orderId}/financial-status")
    public Mono<OrderFinancialStatusResponse> getFinancialStatus(
            @PathVariable String orderId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getOrderFinancialStatusQueryUseCase
                .handle(queryMapper.toFinancialStatusQuery(orderId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/orders/{orderId}/payments/manual")
    public Flux<ManualPaymentResponse> listManualPayments(
            @PathVariable String orderId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listOrderPaymentsQueryUseCase
                .handle(queryMapper.toListPaymentsQuery(orderId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.admin', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/orders/{orderId}/audit")
    public Mono<OrderAuditResponse> getAudit(
            @PathVariable String orderId,
            @RequestParam(required = false) @Min(1) @Max(500) Integer limit,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getOrderAuditQueryUseCase
                .handle(queryMapper.toAuditQuery(orderId, limit, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('order.read', 'order.write', 'ROLE_ORDER_ADMIN', 'ROLE_ARKA_ADMIN')")
    @GetMapping("/orders/{orderId}/amounts")
    public Mono<OrderAmountsResponse> calculateAmounts(
            @PathVariable String orderId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return calculateOrderAmountsQueryUseCase
                .handle(queryMapper.toAmountsQuery(orderId, principal))
                .map(responseMapper::toResponse);
    }
}
