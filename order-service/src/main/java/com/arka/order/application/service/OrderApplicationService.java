package com.arka.order.application.service;

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
import com.arka.order.application.exception.IdempotencyConflictException;
import com.arka.order.application.exception.OrderConflictException;
import com.arka.order.application.exception.OrderNotFoundException;
import com.arka.order.application.exception.OrderValidationException;
import com.arka.order.application.mapper.command.IdempotencySupport;
import com.arka.order.application.mapper.result.OrderResultMapper;
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
import com.arka.order.application.port.out.audit.OrderAuditPort;
import com.arka.order.application.port.out.cache.CheckoutAttemptCachePort;
import com.arka.order.application.port.out.directory.DirectoryCheckoutContext;
import com.arka.order.application.port.out.directory.DirectoryCheckoutPort;
import com.arka.order.application.port.out.external.ActorLegitimacyPort;
import com.arka.order.application.port.out.external.CatalogVariantPort;
import com.arka.order.application.port.out.external.CatalogVariantSnapshot;
import com.arka.order.application.port.out.external.ClockPort;
import com.arka.order.application.port.out.external.InventoryReservationPort;
import com.arka.order.application.port.out.external.InventoryReservationValidation;
import com.arka.order.application.port.out.persistence.CartPersistencePort;
import com.arka.order.application.port.out.persistence.CheckoutAttemptPersistencePort;
import com.arka.order.application.port.out.persistence.IdempotencyRecordPersistencePort;
import com.arka.order.application.port.out.persistence.OrderStatusHistoryPersistencePort;
import com.arka.order.application.port.out.persistence.OutboxPersistencePort;
import com.arka.order.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.order.application.port.out.persistence.PurchaseOrderPersistencePort;
import com.arka.order.application.port.out.security.ActorContext;
import com.arka.order.application.port.out.security.ActorContextProviderPort;
import com.arka.order.application.query.CalculateOrderAmountsQuery;
import com.arka.order.application.query.GetActiveCartQuery;
import com.arka.order.application.query.GetCartQuery;
import com.arka.order.application.query.GetCheckoutAttemptByCorrelationQuery;
import com.arka.order.application.query.GetOrderAuditQuery;
import com.arka.order.application.query.GetOrderFinancialStatusQuery;
import com.arka.order.application.query.GetOrderQuery;
import com.arka.order.application.query.GetOrderTimelineQuery;
import com.arka.order.application.query.ListOrderPaymentsQuery;
import com.arka.order.application.query.ListOrdersQuery;
import com.arka.order.application.result.CartResult;
import com.arka.order.application.result.CheckoutAttemptResult;
import com.arka.order.application.result.ManualPaymentResult;
import com.arka.order.application.result.OrderAmountsResult;
import com.arka.order.application.result.OrderAuditEntryResult;
import com.arka.order.application.result.OrderAuditResult;
import com.arka.order.application.result.OrderFinancialStatusResult;
import com.arka.order.application.result.OrderResult;
import com.arka.order.application.result.OrderStatusHistoryResult;
import com.arka.order.application.result.OrderSummaryResult;
import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.domain.cart.entity.CartItem;
import com.arka.order.domain.cart.entity.CheckoutAttempt;
import com.arka.order.domain.cart.enumtype.CheckoutValidationStatus;
import com.arka.order.domain.cart.service.CartPolicyService;
import com.arka.order.domain.cart.valueobject.ValidatedCheckout;
import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.domain.order.entity.IdempotencyRecord;
import com.arka.order.domain.order.entity.OrderLine;
import com.arka.order.domain.order.entity.OrderStatusHistory;
import com.arka.order.domain.order.enumtype.ManualPaymentStatus;
import com.arka.order.domain.order.enumtype.OrderStatus;
import com.arka.order.domain.order.service.OrderPolicyService;
import com.arka.order.domain.shared.event.DomainEvent;
import com.arka.order.domain.shared.exception.OperationNotPermittedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderApplicationService implements
        CreateCartCommandUseCase,
        AdjustCartItemsCommandUseCase,
        ValidateCheckoutAvailabilityCommandUseCase,
        CreateOrderFromCartCommandUseCase,
        AdjustOrderBeforeCloseCommandUseCase,
        RevalidateOrderConsistencyAfterAdjustmentCommandUseCase,
        CancelOrderCommandUseCase,
        UpdateOrderOperationalStatusCommandUseCase,
        RegisterManualPaymentCommandUseCase,
        ValidateManualPaymentCommandUseCase,
        HandleReservationExpiredCommandUseCase,
        GetActiveCartQueryUseCase,
        GetCartQueryUseCase,
        GetCheckoutAttemptByCorrelationQueryUseCase,
        GetOrderQueryUseCase,
        ListOrdersQueryUseCase,
        GetOrderTimelineQueryUseCase,
        GetOrderFinancialStatusQueryUseCase,
        ListOrderPaymentsQueryUseCase,
        GetOrderAuditQueryUseCase,
        CalculateOrderAmountsQueryUseCase {

    private static final int MAX_OPTIMISTIC_RETRIES = 3;
    private static final String RESERVATION_EXPIRED_CONSUMER = "order.reservation-expired-handler";

    private final CartPersistencePort cartPersistencePort;
    private final CheckoutAttemptPersistencePort checkoutAttemptPersistencePort;
    private final PurchaseOrderPersistencePort purchaseOrderPersistencePort;
    private final OrderStatusHistoryPersistencePort orderStatusHistoryPersistencePort;
    private final IdempotencyRecordPersistencePort idempotencyRecordPersistencePort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ProcessedEventPersistencePort processedEventPersistencePort;
    private final OrderAuditPort orderAuditPort;
    private final CheckoutAttemptCachePort checkoutAttemptCachePort;
    private final ClockPort clockPort;
    private final ActorLegitimacyPort actorLegitimacyPort;
    private final CatalogVariantPort catalogVariantPort;
    private final InventoryReservationPort inventoryReservationPort;
    private final DirectoryCheckoutPort directoryCheckoutPort;
    private final ActorContextProviderPort actorContextProviderPort;
    private final CartPolicyService cartPolicyService;
    private final OrderPolicyService orderPolicyService;
    private final OrderResultMapper resultMapper;
    private final ObjectMapper objectMapper;

    public OrderApplicationService(
            CartPersistencePort cartPersistencePort,
            CheckoutAttemptPersistencePort checkoutAttemptPersistencePort,
            PurchaseOrderPersistencePort purchaseOrderPersistencePort,
            OrderStatusHistoryPersistencePort orderStatusHistoryPersistencePort,
            IdempotencyRecordPersistencePort idempotencyRecordPersistencePort,
            OutboxPersistencePort outboxPersistencePort,
            ProcessedEventPersistencePort processedEventPersistencePort,
            OrderAuditPort orderAuditPort,
            CheckoutAttemptCachePort checkoutAttemptCachePort,
            ClockPort clockPort,
            ActorLegitimacyPort actorLegitimacyPort,
            CatalogVariantPort catalogVariantPort,
            InventoryReservationPort inventoryReservationPort,
            DirectoryCheckoutPort directoryCheckoutPort,
            ActorContextProviderPort actorContextProviderPort,
            CartPolicyService cartPolicyService,
            OrderPolicyService orderPolicyService,
            OrderResultMapper resultMapper,
            ObjectMapper objectMapper) {
        this.cartPersistencePort = cartPersistencePort;
        this.checkoutAttemptPersistencePort = checkoutAttemptPersistencePort;
        this.purchaseOrderPersistencePort = purchaseOrderPersistencePort;
        this.orderStatusHistoryPersistencePort = orderStatusHistoryPersistencePort;
        this.idempotencyRecordPersistencePort = idempotencyRecordPersistencePort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.processedEventPersistencePort = processedEventPersistencePort;
        this.orderAuditPort = orderAuditPort;
        this.checkoutAttemptCachePort = checkoutAttemptCachePort;
        this.clockPort = clockPort;
        this.actorLegitimacyPort = actorLegitimacyPort;
        this.catalogVariantPort = catalogVariantPort;
        this.inventoryReservationPort = inventoryReservationPort;
        this.directoryCheckoutPort = directoryCheckoutPort;
        this.actorContextProviderPort = actorContextProviderPort;
        this.cartPolicyService = cartPolicyService;
        this.orderPolicyService = orderPolicyService;
        this.resultMapper = resultMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Mono<CartResult> handle(CreateCartCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String userId = normalizeRequired(command.userId(), "userId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String operationName = "CreateCart";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadCartResult(organizationId, record.resourceId()),
                        () -> cartPersistencePort.findActiveByOrganizationUser(organizationId, userId)
                                .switchIfEmpty(Mono.defer(() -> cartPersistencePort
                                        .save(Cart.create(organizationId, userId, now()))))
                                .flatMap(cart -> registerMutation(
                                                organizationId,
                                                actorUserId,
                                                operationName,
                                                "Cart",
                                                cart.cartId(),
                                                payload("userId", userId),
                                                cart.pullDomainEvents())
                                        .thenReturn(resultMapper.toResult(cart))),
                        CartResult::cartId,
                        "Cart"));
    }

    @Override
    @Transactional
    public Mono<CartResult> handle(AdjustCartItemsCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String userId = normalizeRequired(command.userId(), "userId");
        String cartId = normalizeRequired(command.cartId(), "cartId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        List<AdjustCartItemInput> items = command.items() == null ? List.of() : command.items();
        if (items.isEmpty()) {
            throw new OrderValidationException("items are required");
        }

        String operationName = "AdjustCartItems";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadCartResult(organizationId, record.resourceId()),
                        () -> updateCartWithRetry(
                                        organizationId,
                                        cartId,
                                        MAX_OPTIMISTIC_RETRIES,
                                        current -> {
                                            cartPolicyService.ensureOwnership(current, organizationId, userId);
                                            return applyCartMutations(current, items.iterator(), organizationId, now());
                                        })
                                .flatMap(updated -> registerMutation(
                                                organizationId,
                                                actorUserId,
                                                operationName,
                                                "Cart",
                                                updated.cartId(),
                                                payload("itemCount", updated.items().size()),
                                                updated.pullDomainEvents())
                                        .thenReturn(resultMapper.toResult(updated))),
                        CartResult::cartId,
                        "Cart"));
    }

    @Override
    @Transactional
    public Mono<CheckoutAttemptResult> handle(ValidateCheckoutAvailabilityCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String userId = normalizeRequired(command.userId(), "userId");
        String cartId = normalizeRequired(command.cartId(), "cartId");
        String correlationId = normalizeRequired(command.checkoutCorrelationId(), "checkoutCorrelationId");
        String addressId = normalizeRequired(command.addressId(), "addressId");
        String countryCode = normalizeCountryCode(command.countryCode());
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");

        String operationName = "ValidateCheckoutAvailability";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadCheckoutAttemptResult(organizationId, record.resourceId()),
                        () -> validateCheckoutWithRetry(
                                        organizationId,
                                        userId,
                                        cartId,
                                        correlationId,
                                        addressId,
                                        countryCode,
                                        MAX_OPTIMISTIC_RETRIES)
                                .flatMap(state -> checkoutAttemptPersistencePort.save(state.checkoutAttempt())
                                        .flatMap(savedAttempt -> checkoutAttemptCachePort
                                                .put(resultMapper.toResult(savedAttempt))
                                                .then(registerMutation(
                                                        organizationId,
                                                        actorUserId,
                                                        operationName,
                                                        "CheckoutAttempt",
                                                        savedAttempt.checkoutAttemptId(),
                                                        payload("checkoutCorrelationId", savedAttempt.checkoutCorrelationId(),
                                                                "validationStatus", savedAttempt.validationStatus().name()),
                                                        state.updatedCart().pullDomainEvents()))
                                                .thenReturn(resultMapper.toResult(savedAttempt)))), 
                        CheckoutAttemptResult::checkoutCorrelationId,
                        "CheckoutAttempt"));
    }

    @Override
    @Transactional
    public Mono<OrderResult> handle(CreateOrderFromCartCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String userId = normalizeRequired(command.userId(), "userId");
        String cartId = normalizeRequired(command.cartId(), "cartId");
        String correlationId = normalizeRequired(command.checkoutCorrelationId(), "checkoutCorrelationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");

        String operationName = "CreateOrderFromCart";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadOrderResult(organizationId, record.resourceId()),
                        () -> purchaseOrderPersistencePort.findByCheckoutCorrelation(organizationId, correlationId)
                                .flatMap(existing -> Mono.just(resultMapper.toResult(existing)))
                                .switchIfEmpty(Mono.defer(() -> createOrderFromValidatedCheckout(
                                        organizationId,
                                        userId,
                                        cartId,
                                        correlationId,
                                        actorUserId))),
                        OrderResult::orderId,
                        "Order"));
    }

    @Override
    @Transactional
    public Mono<OrderResult> handle(AdjustOrderBeforeCloseCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String orderId = normalizeRequired(command.orderId(), "orderId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        List<AdjustOrderLineInput> lines = command.lines() == null ? List.of() : command.lines();
        if (lines.isEmpty()) {
            throw new OrderValidationException("lines are required");
        }

        String operationName = "AdjustOrderBeforeClose";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadOrderResult(organizationId, record.resourceId()),
                        () -> updateOrderWithRetry(
                                        organizationId,
                                        orderId,
                                        MAX_OPTIMISTIC_RETRIES,
                                        current -> {
                                            orderPolicyService.ensureOwnership(current, organizationId);
                                            return toAdjustedOrderLines(current, lines, now())
                                                    .map(adjusted -> current.adjustBeforeClose(adjusted, now()));
                                        })
                                .flatMap(updated -> registerMutation(
                                                organizationId,
                                                actorUserId,
                                                operationName,
                                                "Order",
                                                updated.orderId(),
                                                payload("lineCount", updated.lines().size(), "reason", normalizeOptional(command.reason(), "manual-adjustment")),
                                                updated.pullDomainEvents())
                                        .thenReturn(resultMapper.toResult(updated))),
                        OrderResult::orderId,
                        "Order"));
    }

    @Override
    @Transactional
    public Mono<OrderResult> handle(RevalidateOrderConsistencyAfterAdjustmentCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String orderId = normalizeRequired(command.orderId(), "orderId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");

        String operationName = "RevalidateOrderConsistencyAfterAdjustment";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadOrderResult(organizationId, record.resourceId()),
                        () -> updateOrderWithRetry(
                                        organizationId,
                                        orderId,
                                        MAX_OPTIMISTIC_RETRIES,
                                        current -> {
                                            orderPolicyService.ensureOwnership(current, organizationId);
                                            return Mono.just(current.revalidateConsistencyAfterAdjustment(now()));
                                        })
                                .flatMap(updated -> registerMutation(
                                                organizationId,
                                                actorUserId,
                                                operationName,
                                                "Order",
                                                updated.orderId(),
                                                payload("reason", normalizeOptional(command.reason(), "revalidation")),
                                                updated.pullDomainEvents())
                                        .thenReturn(resultMapper.toResult(updated))),
                        OrderResult::orderId,
                        "Order"));
    }

    @Override
    @Transactional
    public Mono<OrderResult> handle(CancelOrderCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String orderId = normalizeRequired(command.orderId(), "orderId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");

        String operationName = "CancelOrder";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadOrderResult(organizationId, record.resourceId()),
                        () -> transitionOrderStatus(
                                organizationId,
                                orderId,
                                OrderStatus.CANCELLED,
                                normalizeOptional(command.reason(), "cancelled"),
                                actorUserId),
                        OrderResult::orderId,
                        "Order"));
    }

    @Override
    @Transactional
    public Mono<OrderResult> handle(UpdateOrderOperationalStatusCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String orderId = normalizeRequired(command.orderId(), "orderId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        OrderStatus targetStatus = parseOrderStatus(command.targetStatus());
        orderPolicyService.ensureMvpTransition(targetStatus);

        String operationName = "UpdateOrderOperationalStatus";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadOrderResult(organizationId, record.resourceId()),
                        () -> transitionOrderStatus(
                                organizationId,
                                orderId,
                                targetStatus,
                                normalizeOptional(command.reason(), "manual-transition"),
                                actorUserId),
                        OrderResult::orderId,
                        "Order"));
    }

    @Override
    @Transactional
    public Mono<OrderResult> handle(RegisterManualPaymentCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String orderId = normalizeRequired(command.orderId(), "orderId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");

        String operationName = "RegisterManualPayment";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadOrderResult(organizationId, record.resourceId()),
                        () -> updateOrderWithRetry(
                                        organizationId,
                                        orderId,
                                        MAX_OPTIMISTIC_RETRIES,
                                        current -> {
                                            orderPolicyService.ensureOwnership(current, organizationId);
                                            return Mono.just(current.registerManualPayment(
                                                    normalizeRequired(command.paymentReference(), "paymentReference"),
                                                    command.amount(),
                                                    normalizeRequired(command.method(), "method"),
                                                    normalizeRequired(command.supportReference(), "supportReference"),
                                                    command.receivedAt(),
                                                    now()));
                                        })
                                .flatMap(updated -> registerMutation(
                                                organizationId,
                                                actorUserId,
                                                operationName,
                                                "Order",
                                                updated.orderId(),
                                                payload("paymentReference", command.paymentReference(), "amount", command.amount()),
                                                updated.pullDomainEvents())
                                        .thenReturn(resultMapper.toResult(updated))),
                        OrderResult::orderId,
                        "Order"));
    }

    @Override
    @Transactional
    public Mono<OrderResult> handle(ValidateManualPaymentCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String orderId = normalizeRequired(command.orderId(), "orderId");
        String paymentRecordId = normalizeRequired(command.paymentRecordId(), "paymentRecordId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        ManualPaymentStatus targetStatus = parseManualPaymentStatus(command.targetStatus());

        if (targetStatus == ManualPaymentStatus.REGISTERED) {
            throw new OrderValidationException("payment status REGISTERED is not a validation target");
        }

        String operationName = "ValidateManualPayment";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadOrderResult(organizationId, record.resourceId()),
                        () -> updateOrderWithRetry(
                                        organizationId,
                                        orderId,
                                        MAX_OPTIMISTIC_RETRIES,
                                        current -> {
                                            orderPolicyService.ensureOwnership(current, organizationId);
                                            return Mono.just(current.updateManualPaymentStatus(paymentRecordId, targetStatus, now()));
                                        })
                                .flatMap(updated -> registerMutation(
                                                organizationId,
                                                actorUserId,
                                                operationName,
                                                "Order",
                                                updated.orderId(),
                                                payload("paymentRecordId", paymentRecordId,
                                                        "targetStatus", targetStatus.name(),
                                                        "reason", normalizeOptional(command.reason(), "manual-payment-validation")),
                                                updated.pullDomainEvents())
                                        .thenReturn(resultMapper.toResult(updated))),
                        OrderResult::orderId,
                        "Order"));
    }

    @Override
    @Transactional
    public Mono<CartResult> handle(HandleReservationExpiredCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");

        String cartId = normalizeRequired(command.cartId(), "cartId");
        String reservationId = normalizeRequired(command.reservationId(), "reservationId");
        String eventId = normalizeRequired(command.eventId(), "eventId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(processedEventPersistencePort.existsByEventAndConsumer(eventId, RESERVATION_EXPIRED_CONSUMER))
                .flatMap(alreadyProcessed -> {
                    if (alreadyProcessed) {
                        return loadCartResult(organizationId, cartId);
                    }
                    return updateCartWithRetry(
                                    organizationId,
                                    cartId,
                                    MAX_OPTIMISTIC_RETRIES,
                                    current -> {
                                        if (!current.organizationId().equals(organizationId)) {
                                            return Mono.error(new OperationNotPermittedException("organization isolation violated for cart"));
                                        }
                                        Cart updated = current;
                                        for (CartItem item : current.items()) {
                                            if (reservationId.equals(item.reservationId())) {
                                                updated = updated.removeItem(item.cartItemId(), now());
                                            }
                                        }
                                        return Mono.just(updated);
                                    })
                            .flatMap(updated -> processedEventPersistencePort.registerProcessed(eventId, RESERVATION_EXPIRED_CONSUMER)
                                    .then(registerMutation(
                                            organizationId,
                                            actorUserId,
                                            "HandleReservationExpired",
                                            "Cart",
                                            updated.cartId(),
                                            payload("reservationId", reservationId, "eventId", eventId),
                                            updated.pullDomainEvents()))
                                    .thenReturn(resultMapper.toResult(updated)));
                });
    }

    @Override
    public Mono<CartResult> handle(GetActiveCartQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String userId = normalizeRequired(query.userId(), "userId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(cartPersistencePort.findActiveByOrganizationUser(organizationId, userId)
                        .switchIfEmpty(Mono.error(new OrderNotFoundException("active cart not found")))
                        .map(resultMapper::toResult));
    }

    @Override
    public Mono<CartResult> handle(GetCartQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String cartId = normalizeRequired(query.cartId(), "cartId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadCart(organizationId, cartId))
                .flatMap(cart -> {
                    if (!cart.organizationId().equals(organizationId)) {
                        return Mono.error(new OperationNotPermittedException("organization isolation violated for cart"));
                    }
                    return Mono.just(resultMapper.toResult(cart));
                });
    }

    @Override
    public Mono<CheckoutAttemptResult> handle(GetCheckoutAttemptByCorrelationQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String correlationId = normalizeRequired(query.checkoutCorrelationId(), "checkoutCorrelationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(checkoutAttemptCachePort.findByCorrelation(organizationId, correlationId)
                        .switchIfEmpty(checkoutAttemptPersistencePort.findByCorrelation(organizationId, correlationId)
                                .switchIfEmpty(Mono.error(new OrderNotFoundException("checkout attempt not found")))
                                .map(resultMapper::toResult)
                                .flatMap(result -> checkoutAttemptCachePort.put(result).thenReturn(result))))
                .flatMap(result -> {
                    if (!organizationId.equals(result.organizationId())) {
                        return Mono.error(new OperationNotPermittedException("organization isolation violated for checkout attempt"));
                    }
                    return Mono.just(result);
                });
    }

    @Override
    public Mono<OrderResult> handle(GetOrderQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String orderId = normalizeRequired(query.orderId(), "orderId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadOrder(organizationId, orderId))
                .flatMap(order -> {
                    orderPolicyService.ensureOwnership(order, organizationId);
                    return Mono.just(resultMapper.toResult(order));
                });
    }

    @Override
    public Flux<OrderSummaryResult> handle(ListOrdersQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String status = query.status() == null || query.status().isBlank()
                ? null
                : parseOrderStatus(query.status()).name();
        int limit = query.limit() == null ? 100 : requirePositive(query.limit(), "limit");

        return ensureActorAccess(organizationId, actorUserId, true)
                .thenMany(purchaseOrderPersistencePort.listByOrganizationStatus(
                                organizationId,
                                status,
                                query.createdFrom(),
                                query.createdTo(),
                                limit)
                        .map(resultMapper::toSummaryResult));
    }

    @Override
    public Flux<OrderStatusHistoryResult> handle(GetOrderTimelineQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String orderId = normalizeRequired(query.orderId(), "orderId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadOrder(organizationId, orderId)
                        .doOnNext(order -> orderPolicyService.ensureOwnership(order, organizationId)))
                .thenMany(orderStatusHistoryPersistencePort.findByOrder(organizationId, orderId)
                        .map(resultMapper::toResult));
    }

    @Override
    public Mono<OrderFinancialStatusResult> handle(GetOrderFinancialStatusQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String orderId = normalizeRequired(query.orderId(), "orderId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadOrder(organizationId, orderId))
                .flatMap(order -> {
                    orderPolicyService.ensureOwnership(order, organizationId);
                    return Mono.just(resultMapper.toFinancialStatusResult(order));
                });
    }

    @Override
    public Flux<ManualPaymentResult> handle(ListOrderPaymentsQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String orderId = normalizeRequired(query.orderId(), "orderId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadOrder(organizationId, orderId)
                        .doOnNext(order -> orderPolicyService.ensureOwnership(order, organizationId)))
                .flatMapMany(order -> Flux.fromIterable(order.payments()).map(resultMapper::toResult));
    }

    @Override
    public Mono<OrderAuditResult> handle(GetOrderAuditQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String orderId = normalizeRequired(query.orderId(), "orderId");
        int limit = query.limit() == null ? 100 : requirePositive(query.limit(), "limit");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(orderAuditPort.findByOrder(organizationId, orderId, limit).collectList())
                .map(entries -> new OrderAuditResult(organizationId, orderId, entries));
    }

    @Override
    public Mono<OrderAmountsResult> handle(CalculateOrderAmountsQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");

        String orderId = normalizeRequired(query.orderId(), "orderId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadOrder(organizationId, orderId))
                .flatMap(order -> {
                    orderPolicyService.ensureOwnership(order, organizationId);
                    BigDecimal paid = order.paidAmount();
                    BigDecimal pending = order.totalAmount().subtract(paid);
                    if (pending.signum() < 0) {
                        pending = BigDecimal.ZERO;
                    }
                    return Mono.just(new OrderAmountsResult(
                            order.orderId(),
                            order.subtotal(),
                            order.totalAmount(),
                            paid,
                            pending));
                });
    }

    private Mono<OrderResult> createOrderFromValidatedCheckout(
            String organizationId,

            String userId,
            String cartId,
            String checkoutCorrelationId,
            String actorUserId) {
        return loadCart(organizationId, cartId)
                .flatMap(cart -> {
                    cartPolicyService.ensureOwnership(cart, organizationId, userId);
                    cart.ensureAllItemsHaveConfirmedReservation();
                    return checkoutAttemptPersistencePort.findByCorrelation(organizationId, checkoutCorrelationId)
                            .switchIfEmpty(Mono.error(new OrderValidationException("validated checkout not found")))
                            .flatMap(attempt -> {
                                if (!attempt.cartId().equals(cart.cartId())) {
                                    return Mono.error(new OrderValidationException("checkout correlation does not belong to cart"));
                                }
                                if (attempt.validationStatus() != CheckoutValidationStatus.VALID) {
                                    return Mono.error(new OrderValidationException("checkout validation is INVALID"));
                                }
                                return toOrderLinesFromCart(cart, now())
                                        .flatMap(lines -> {
                                            Order order = Order.createFromValidatedCart(
                                                    organizationId,
                                                    userId,
                                                    cart.cartId(),
                                                    checkoutCorrelationId,
                                                    attempt.addressId(),
                                                    attempt.countryCode(),
                                                    attempt.regionalPolicyVersion(),
                                                    attempt.policyCurrency(),
                                                    lines,
                                                    now());

                                            return purchaseOrderPersistencePort.save(order)
                                                    .flatMap(savedOrder -> {
                                                        Cart converted = cart.convertToOrder(savedOrder.orderId(), now());
                                                        return cartPersistencePort
                                                                .updateWithExpectedVersion(converted, cart.version())
                                                                .flatMap(updated -> updated
                                                                        ? Mono.empty()
                                                                        : Mono.error(new OrderConflictException(
                                                                                "cart changed while creating order; retry request")))
                                                                .then(saveStatusHistory(savedOrder, null, savedOrder.status(), "created-from-validated-cart", actorUserId))
                                                                .then(registerMutation(
                                                                        organizationId,
                                                                        actorUserId,
                                                                        "CreateOrderFromCart",
                                                                        "Order",
                                                                        savedOrder.orderId(),
                                                                        payload("checkoutCorrelationId", checkoutCorrelationId,
                                                                                "orderNumber", savedOrder.orderNumber()),
                                                                        savedOrder.pullDomainEvents()))
                                                                .thenReturn(resultMapper.toResult(savedOrder));
                                                    });
                                        });
                            });
                });
    }

    private Mono<OrderResult> transitionOrderStatus(
            String organizationId,

            String orderId,
            OrderStatus targetStatus,
            String reason,
            String actorUserId) {
        return transitionOrderStatusWithRetry(
                        organizationId,
                        orderId,
                        targetStatus,
                        reason,
                        MAX_OPTIMISTIC_RETRIES)
                .flatMap(state -> saveStatusHistory(
                                state.updatedOrder(),
                                state.fromStatus(),
                                targetStatus,
                                reason,
                                actorUserId)
                        .then(registerMutation(
                                organizationId,
                                actorUserId,
                                "UpdateOrderOperationalStatus",
                                "Order",
                                state.updatedOrder().orderId(),
                                payload("toStatus", targetStatus.name(), "reason", reason),
                                state.updatedOrder().pullDomainEvents()))
                        .thenReturn(resultMapper.toResult(state.updatedOrder())));
    }

    private Mono<OrderTransitionState> transitionOrderStatusWithRetry(
            String organizationId,

            String orderId,
            OrderStatus targetStatus,
            String reason,
            int retriesLeft) {
        return loadOrder(organizationId, orderId)
                .flatMap(current -> {
                    orderPolicyService.ensureOwnership(current, organizationId);
                    Order updated = current.updateOperationalStatus(targetStatus, reason, now());
                    return purchaseOrderPersistencePort.updateWithExpectedVersion(updated, current.version())
                            .flatMap(success -> {
                                if (success) {
                                    return Mono.just(new OrderTransitionState(updated, current.status()));
                                }
                                if (retriesLeft <= 0) {
                                    return Mono.error(new OrderConflictException(
                                            "order changed concurrently while transitioning status; retry request"));
                                }
                                return transitionOrderStatusWithRetry(
                                        organizationId,
                                        orderId,
                                        targetStatus,
                                        reason,
                                        retriesLeft - 1);
                            });
                });
    }

    private Mono<CartMutationState> validateCheckoutWithRetry(
            String organizationId,

            String userId,
            String cartId,
            String checkoutCorrelationId,
            String addressId,
            String countryCode,
            int retriesLeft) {
        return loadCart(organizationId, cartId)
                .flatMap(current -> {
                    cartPolicyService.ensureOwnership(current, organizationId, userId);
                    if (current.items().isEmpty()) {
                        return Mono.error(new OrderValidationException("cart must contain at least one item"));
                    }

                    return collectCheckoutRejectionReasons(organizationId, current)
                            .flatMap(reasons -> directoryCheckoutPort.resolveCheckoutContext(
                                            organizationId,
                                            addressId,
                                            countryCode)
                                    .map(context -> mergeDirectoryReasons(reasons, context))
                                    .flatMap(bundle -> {
                                        CheckoutValidationStatus validationStatus = bundle.reasons().isEmpty()
                                                ? CheckoutValidationStatus.VALID
                                                : CheckoutValidationStatus.INVALID;

                                        ValidatedCheckout validatedCheckout = new ValidatedCheckout(
                                                checkoutCorrelationId,
                                                cartId,
                                                validationStatus,
                                                organizationId,
                                                addressId,
                                                countryCode,
                                                bundle.context().regionalPolicyVersion(),
                                                bundle.context().policyCurrency(),
                                                bundle.reasons(),
                                                now());

                                        Cart updatedCart = current.markCheckoutValidated(validatedCheckout, now());
                                        CheckoutAttempt checkoutAttempt = new CheckoutAttempt(
                                                UUID.randomUUID().toString(),
                                                organizationId,
                                                userId,
                                                cartId,
                                                checkoutCorrelationId,
                                                validationStatus,
                                                addressId,
                                                countryCode,
                                                bundle.context().regionalPolicyVersion(),
                                                bundle.context().policyCurrency(),
                                                bundle.reasons(),
                                                now(),
                                                now());

                                        return cartPersistencePort.updateWithExpectedVersion(updatedCart, current.version())
                                                .flatMap(updated -> {
                                                    if (!updated) {
                                                        if (retriesLeft <= 0) {
                                                            return Mono.error(new OrderConflictException(
                                                                    "cart changed during checkout validation; retry request"));
                                                        }
                                                        return validateCheckoutWithRetry(
                                                                organizationId,
                                                                userId,
                                                                cartId,
                                                                checkoutCorrelationId,
                                                                addressId,
                                                                countryCode,
                                                                retriesLeft - 1);
                                                    }
                                                    return Mono.just(new CartMutationState(updatedCart, checkoutAttempt));
                                                });
                                    }));
                });
    }

    private Mono<List<String>> collectCheckoutRejectionReasons(String organizationId, Cart cart) {
        return Flux.fromIterable(cart.items())
                .concatMap(item -> collectItemCheckoutIssues(organizationId, item))
                .collectList();
    }

    private Mono<String> collectItemCheckoutIssues(String organizationId, CartItem item) {
        return catalogVariantPort.resolveVariant(organizationId, item.variantId(), item.sku())
                .flatMap(snapshot -> {
                    if (!snapshot.sellable()) {
                        return Mono.just("oferta_no_vendible:" + item.sku());
                    }
                    return inventoryReservationPort.validateReservation(
                                    organizationId,
                                    item.reservationId(),
                                    item.sku(),
                                    item.qty())
                            .map(validation -> validation.reservationConfirmed() && validation.commitableAvailable()
                                    ? ""
                                    : "reserva_no_confirmada:" + item.sku());
                })
                .defaultIfEmpty("oferta_no_resuelta:" + item.sku());
    }

    private CheckoutValidationBundle mergeDirectoryReasons(List<String> reasons, DirectoryCheckoutContext context) {
        List<String> merged = new ArrayList<>();
        for (String reason : reasons) {
            if (reason != null && !reason.isBlank()) {
                merged.add(reason);
            }
        }
        if (!context.policyActive()) {
            merged.add("configuracion_pais_no_disponible");
        }
        if (!context.addressValid()) {
            merged.add("direccion_checkout_invalida");
        }
        return new CheckoutValidationBundle(context, merged);
    }

    private Mono<List<OrderLine>> toOrderLinesFromCart(Cart cart, Instant now) {
        return Flux.fromIterable(cart.items())
                .concatMap(item -> catalogVariantPort.resolveVariant(cart.organizationId(), item.variantId(), item.sku())
                        .switchIfEmpty(Mono.error(new OrderValidationException("variant not found for cart item " + item.sku())))
                        .flatMap(snapshot -> validateReservationForLine(cart.organizationId(), item, snapshot)
                                .map(validation -> new OrderLine(
                                        UUID.randomUUID().toString(),
                                        "PENDING_ORDER_ID",
                                        cart.organizationId(),
                                        item.variantId(),
                                        item.sku(),
                                        item.qty(),
                                        snapshot.unitPrice(),
                                        snapshot.currency(),
                                        item.reservationId(),
                                        validation.reservationConfirmed(),
                                        null,
                                        now,
                                        now))))
                .collectList();
    }

    private Mono<InventoryReservationValidation> validateReservationForLine(
            String organizationId,
            CartItem item,
            CatalogVariantSnapshot snapshot) {
        if (!snapshot.sellable()) {
            return Mono.error(new OrderValidationException("variant is not sellable for sku " + item.sku()));
        }
        return inventoryReservationPort.validateReservation(organizationId, item.reservationId(), item.sku(), item.qty())
                .flatMap(validation -> {
                    if (!validation.reservationConfirmed() || !validation.commitableAvailable()) {
                        return Mono.error(new OrderValidationException("reservation is not valid for sku " + item.sku()));
                    }
                    return Mono.just(validation);
                });
    }

    private Mono<List<OrderLine>> toAdjustedOrderLines(Order order, List<AdjustOrderLineInput> lines, Instant now) {
        return Flux.fromIterable(lines)
                .concatMap(line -> {
                    String variantId = normalizeRequired(line.variantId(), "variantId");
                    String sku = normalizeSku(line.sku());
                    int qty = requirePositive(line.qty(), "qty");
                    String reservationId = normalizeRequired(line.reservationId(), "reservationId");

                    return catalogVariantPort.resolveVariant(order.organizationId(), variantId, sku)
                            .switchIfEmpty(Mono.error(new OrderValidationException("variant not found for sku " + sku)))
                            .flatMap(snapshot -> {
                                BigDecimal unitPrice = line.unitPrice() == null ? snapshot.unitPrice() : line.unitPrice();
                                String currency = line.currency() == null || line.currency().isBlank()
                                        ? snapshot.currency()
                                        : line.currency().toUpperCase(Locale.ROOT);
                                return inventoryReservationPort.validateReservation(order.organizationId(), reservationId, sku, qty)
                                        .flatMap(validation -> {
                                            if (!validation.reservationConfirmed() || !validation.commitableAvailable()) {
                                                return Mono.error(new OrderValidationException(
                                                        "reservation is not valid for adjusted line " + sku));
                                            }
                                            return Mono.just(new OrderLine(
                                                    line.orderLineId() == null || line.orderLineId().isBlank()
                                                            ? UUID.randomUUID().toString()
                                                            : line.orderLineId().trim(),
                                                    order.orderId(),
                                                    order.organizationId(),
                                                    variantId,
                                                    sku,
                                                    qty,
                                                    unitPrice,
                                                    currency,
                                                    reservationId,
                                                    true,
                                                    null,
                                                    now,
                                                    now));
                                        });
                            });
                })
                .collectList();
    }

    private Mono<Cart> applyCartMutations(
            Cart cart,
            Iterator<AdjustCartItemInput> iterator,
            String organizationId,
            Instant changedAt) {
        if (!iterator.hasNext()) {
            return Mono.just(cart);
        }
        AdjustCartItemInput input = iterator.next();
        return applySingleCartMutation(cart, input, organizationId, changedAt)
                .flatMap(next -> applyCartMutations(next, iterator, organizationId, changedAt));
    }

    private Mono<Cart> applySingleCartMutation(
            Cart cart,
            AdjustCartItemInput input,
            String organizationId,
            Instant changedAt) {
        String operation = normalizeOptional(input.operation(), "UPSERT").toUpperCase(Locale.ROOT);
        if ("REMOVE".equals(operation)) {
            return Mono.just(cart.removeItem(normalizeRequired(input.cartItemId(), "cartItemId"), changedAt));
        }
        String variantId = normalizeRequired(input.variantId(), "variantId");
        String sku = normalizeSku(input.sku());
        int qty = requirePositive(input.qty(), "qty");
        String reservationId = normalizeRequired(input.reservationId(), "reservationId");

        return catalogVariantPort.resolveVariant(organizationId, variantId, sku)
                .switchIfEmpty(Mono.error(new OrderValidationException("variant not found for sku " + sku)))
                .flatMap(snapshot -> {
                    if (!snapshot.sellable()) {
                        return Mono.error(new OrderValidationException("variant is not sellable for sku " + sku));
                    }
                    BigDecimal unitPrice = input.unitPrice() == null ? snapshot.unitPrice() : input.unitPrice();
                    String currency = input.currency() == null || input.currency().isBlank()
                            ? snapshot.currency()
                            : input.currency().toUpperCase(Locale.ROOT);

                    return inventoryReservationPort.validateReservation(organizationId, reservationId, sku, qty)
                            .flatMap(validation -> {
                                if (!validation.reservationConfirmed() || !validation.commitableAvailable()) {
                                    return Mono.error(new OrderValidationException("reservation is not valid for sku " + sku));
                                }
                                return Mono.just(cart.addOrUpdateItem(
                                        input.cartItemId(),
                                        variantId,
                                        sku,
                                        qty,
                                        unitPrice,
                                        currency,
                                        reservationId,
                                        true,
                                        changedAt));
                            });
                });
    }

    private Mono<Cart> updateCartWithRetry(
            String organizationId,
            String cartId,
            int retriesLeft,
            Function<Cart, Mono<Cart>> mutator) {
        return loadCart(organizationId, cartId)
                .flatMap(current -> mutator.apply(current)
                        .flatMap(updated -> cartPersistencePort.updateWithExpectedVersion(updated, current.version())
                                .flatMap(success -> {
                                    if (success) {
                                        return Mono.just(updated);
                                    }
                                    if (retriesLeft <= 0) {
                                        return Mono.error(new OrderConflictException(
                                                "cart changed concurrently; retry request"));
                                    }
                                    return updateCartWithRetry(organizationId, cartId, retriesLeft - 1, mutator);
                                })));
    }

    private Mono<Order> updateOrderWithRetry(
            String organizationId,
            String orderId,
            int retriesLeft,
            Function<Order, Mono<Order>> mutator) {
        return loadOrder(organizationId, orderId)
                .flatMap(current -> mutator.apply(current)
                        .flatMap(updated -> purchaseOrderPersistencePort.updateWithExpectedVersion(updated, current.version())
                                .flatMap(success -> {
                                    if (success) {
                                        return Mono.just(updated);
                                    }
                                    if (retriesLeft <= 0) {
                                        return Mono.error(new OrderConflictException(
                                                "order changed concurrently; retry request"));
                                    }
                                    return updateOrderWithRetry(organizationId, orderId, retriesLeft - 1, mutator);
                                })));
    }

    private Mono<OrderStatusHistory> saveStatusHistory(
            Order order,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String reason,
            String actorUserId) {
        OrderStatusHistory history = new OrderStatusHistory(
                UUID.randomUUID().toString(),
                order.orderId(),
                order.organizationId(),
                actorUserId,
                fromStatus,
                toStatus,
                reason,
                now());
        return orderStatusHistoryPersistencePort.save(history);
    }

    private Mono<Void> ensureActorAccess(
            String organizationId,

            String actorUserId,
            boolean requiresOrderAdmin) {
        return actorContextProviderPort.currentActor()
                .flatMap(context -> {
                    validateActorContext(context, organizationId, actorUserId, requiresOrderAdmin);
                    if (context.trustedService()) {
                        return Mono.empty();
                    }
                    return actorLegitimacyPort.isLegitimate(actorUserId)
                            .flatMap(legitimate -> legitimate
                                    ? Mono.empty()
                                    : Mono.error(new OperationNotPermittedException("actor is not legitimate for order context")));
                });
    }

    private void validateActorContext(
            ActorContext context,
            String organizationId,

            String actorUserId,
            boolean requiresOrderAdmin) {
        if (context.trustedService()) {
            return;
        }
        if (!organizationId.equals(context.organizationId())) {
            throw new OperationNotPermittedException("organization isolation violated");
        }
        if (!organizationId.equals(context.organizationId())) {
            throw new OperationNotPermittedException("organization isolation violated");
        }
        boolean actorMatches = actorUserId.equals(context.userId());
        if (!actorMatches && !context.orderAdmin()) {
            throw new OperationNotPermittedException("actor does not own operation context");
        }
        if (requiresOrderAdmin && !context.orderAdmin()) {
            throw new OperationNotPermittedException("order admin role is required");
        }
    }

    private Mono<Cart> loadCart(String organizationId, String cartId) {
        return cartPersistencePort.findById(organizationId, cartId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("cart not found")));
    }

    private Mono<CartResult> loadCartResult(String organizationId, String cartId) {
        return loadCart(organizationId, cartId).map(resultMapper::toResult);
    }

    private Mono<CheckoutAttemptResult> loadCheckoutAttemptResult(String organizationId, String checkoutCorrelationId) {
        return checkoutAttemptPersistencePort.findByCorrelation(organizationId, checkoutCorrelationId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("checkout attempt not found")))
                .map(resultMapper::toResult);
    }

    private Mono<Order> loadOrder(String organizationId, String orderId) {
        return purchaseOrderPersistencePort.findById(organizationId, orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("order not found")));
    }

    private Mono<OrderResult> loadOrderResult(String organizationId, String orderId) {
        return loadOrder(organizationId, orderId).map(resultMapper::toResult);
    }

    private Mono<Void> registerMutation(
            String organizationId,

            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String payload,
            List<DomainEvent> domainEvents) {
        return orderAuditPort.record(
                        organizationId,
                        actorUserId,
                        actionType,
                        targetType,
                        targetId,
                        "SUCCESS",
                        payload)
                .then(outboxPersistencePort.storeAll(domainEvents));
    }

    private <T> Mono<T> executeIdempotent(
            String organizationId,
            String operationName,
            String idempotencyKey,
            String requestHash,
            Function<IdempotencyRecord, Mono<T>> replayLoader,
            Supplier<Mono<T>> action,
            Function<T, String> resourceIdExtractor,
            String resourceType) {
        String normalizedKey = IdempotencySupport.normalizeKey(idempotencyKey);

        return idempotencyRecordPersistencePort
                .findByOrganizationOperationAndKey(organizationId, operationName, normalizedKey)
                .flatMap(existing -> {
                    if (!existing.requestHash().equals(requestHash)) {
                        return Mono.error(new IdempotencyConflictException(
                                "idempotency key already used with different payload"));
                    }
                    return replayLoader.apply(existing)
                            .switchIfEmpty(action.get());
                })
                .switchIfEmpty(Mono.defer(() -> action.get()
                        .flatMap(result -> idempotencyRecordPersistencePort
                                .save(new IdempotencyRecord(
                                        UUID.randomUUID().toString(),
                                        organizationId,
                                        operationName,
                                        normalizedKey,
                                        requestHash,
                                        resourceType,
                                        resourceIdExtractor.apply(result),
                                        200,
                                        now(),
                                        now()))
                                .thenReturn(result))));
    }

    private OrderStatus parseOrderStatus(String rawStatus) {
        String normalized = normalizeRequired(rawStatus, "targetStatus").toUpperCase(Locale.ROOT);
        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new OrderValidationException("unsupported order status: " + rawStatus);
        }
    }

    private ManualPaymentStatus parseManualPaymentStatus(String rawStatus) {
        String normalized = normalizeRequired(rawStatus, "targetStatus").toUpperCase(Locale.ROOT);
        try {
            return ManualPaymentStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new OrderValidationException("unsupported manual payment status: " + rawStatus);
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new OrderValidationException(fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeOptional(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String normalizeSku(String sku) {
        return normalizeRequired(sku, "sku").toUpperCase(Locale.ROOT);
    }

    private String normalizeCountryCode(String countryCode) {
        String normalized = normalizeRequired(countryCode, "countryCode").toUpperCase(Locale.ROOT);
        if (normalized.length() != 2) {
            throw new OrderValidationException("countryCode must have length 2");
        }
        return normalized;
    }

    private int requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new OrderValidationException(fieldName + " must be greater than zero");
        }
        return value;
    }

    private Instant now() {
        return clockPort.now();
    }

    private String payload(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return "{}";
        }
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("payload keyValues must have even length");
        }

        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = keyValues[i + 1];
            values.put(key, value);
        }

        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            return values.toString();
        }
    }

    private record CheckoutValidationBundle(
            DirectoryCheckoutContext context,
            List<String> reasons) {}

    private record CartMutationState(
            Cart updatedCart,
            CheckoutAttempt checkoutAttempt) {}

    private record OrderTransitionState(
            Order updatedOrder,
            OrderStatus fromStatus) {}
}
