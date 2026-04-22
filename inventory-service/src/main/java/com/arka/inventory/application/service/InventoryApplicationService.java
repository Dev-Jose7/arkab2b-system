package com.arka.inventory.application.service;

import com.arka.inventory.application.command.ConfirmReservationCommand;
import com.arka.inventory.application.command.CreateWarehouseCommand;
import com.arka.inventory.application.command.ExpireReservationsCommand;
import com.arka.inventory.application.command.InitializeStockItemCommand;
import com.arka.inventory.application.command.RecalculateCommitableAvailabilityCommand;
import com.arka.inventory.application.command.ReleaseReservationCommand;
import com.arka.inventory.application.command.ReserveStockCommand;
import com.arka.inventory.application.command.UpdateOperationalStockCommand;
import com.arka.inventory.application.command.UpdateStockItemStatusCommand;
import com.arka.inventory.application.exception.IdempotencyConflictException;
import com.arka.inventory.application.exception.InventoryConflictException;
import com.arka.inventory.application.exception.InventoryResourceNotFoundException;
import com.arka.inventory.application.exception.InventoryValidationException;
import com.arka.inventory.application.mapper.command.IdempotencySupport;
import com.arka.inventory.application.mapper.result.InventoryResultMapper;
import com.arka.inventory.application.port.in.ConfirmReservationCommandUseCase;
import com.arka.inventory.application.port.in.CreateWarehouseCommandUseCase;
import com.arka.inventory.application.port.in.ExpireReservationsCommandUseCase;
import com.arka.inventory.application.port.in.GetCommitableAvailabilityQueryUseCase;
import com.arka.inventory.application.port.in.GetInventoryAuditQueryUseCase;
import com.arka.inventory.application.port.in.GetLowStockQueryUseCase;
import com.arka.inventory.application.port.in.GetStockItemQueryUseCase;
import com.arka.inventory.application.port.in.GetStockMovementsQueryUseCase;
import com.arka.inventory.application.port.in.InitializeStockItemCommandUseCase;
import com.arka.inventory.application.port.in.ListReservationsByCartQueryUseCase;
import com.arka.inventory.application.port.in.ListStockByWarehouseQueryUseCase;
import com.arka.inventory.application.port.in.RecalculateCommitableAvailabilityCommandUseCase;
import com.arka.inventory.application.port.in.ReleaseReservationCommandUseCase;
import com.arka.inventory.application.port.in.ReserveStockCommandUseCase;
import com.arka.inventory.application.port.in.ResolveCheckoutAvailabilityQueryUseCase;
import com.arka.inventory.application.port.in.UpdateOperationalStockCommandUseCase;
import com.arka.inventory.application.port.in.UpdateStockItemStatusCommandUseCase;
import com.arka.inventory.application.port.in.ValidateReservationReferenceQueryUseCase;
import com.arka.inventory.application.port.out.audit.InventoryAuditPort;
import com.arka.inventory.application.port.out.cache.CommitableAvailabilityCachePort;
import com.arka.inventory.application.port.out.directory.OrganizationDirectoryPort;
import com.arka.inventory.application.port.out.external.ActorLegitimacyPort;
import com.arka.inventory.application.port.out.external.CatalogSkuPort;
import com.arka.inventory.application.port.out.external.ClockPort;
import com.arka.inventory.application.port.out.external.OrderReferencePort;
import com.arka.inventory.application.port.out.persistence.IdempotencyRecordPersistencePort;
import com.arka.inventory.application.port.out.persistence.OutboxPersistencePort;
import com.arka.inventory.application.port.out.persistence.ReservationLedgerPersistencePort;
import com.arka.inventory.application.port.out.persistence.StockItemPersistencePort;
import com.arka.inventory.application.port.out.persistence.StockMovementPersistencePort;
import com.arka.inventory.application.port.out.persistence.StockReservationPersistencePort;
import com.arka.inventory.application.port.out.persistence.WarehousePersistencePort;
import com.arka.inventory.application.port.out.security.ActorContext;
import com.arka.inventory.application.port.out.security.ActorContextProviderPort;
import com.arka.inventory.application.query.GetCommitableAvailabilityQuery;
import com.arka.inventory.application.query.GetInventoryAuditQuery;
import com.arka.inventory.application.query.GetLowStockQuery;
import com.arka.inventory.application.query.GetStockItemQuery;
import com.arka.inventory.application.query.GetStockMovementsQuery;
import com.arka.inventory.application.query.ListReservationsByCartQuery;
import com.arka.inventory.application.query.ListStockByWarehouseQuery;
import com.arka.inventory.application.query.ResolveCheckoutAvailabilityQuery;
import com.arka.inventory.application.query.ValidateReservationReferenceQuery;
import com.arka.inventory.application.result.CheckoutAvailabilityResult;
import com.arka.inventory.application.result.CommitableAvailabilityResult;
import com.arka.inventory.application.result.ExpiredReservationsResult;
import com.arka.inventory.application.result.InventoryAuditResult;
import com.arka.inventory.application.result.StockItemResult;
import com.arka.inventory.application.result.StockMovementResult;
import com.arka.inventory.application.result.StockReservationResult;
import com.arka.inventory.application.result.WarehouseResult;
import com.arka.inventory.application.result.ReservationValidationResult;
import com.arka.inventory.domain.inventorybalance.aggregate.InventoryBalance;
import com.arka.inventory.domain.inventorybalance.entity.IdempotencyRecord;
import com.arka.inventory.domain.inventorybalance.entity.ReservationLedger;
import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.domain.inventorybalance.entity.StockMovement;
import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.domain.inventorybalance.entity.Warehouse;
import com.arka.inventory.domain.inventorybalance.enumtype.StockItemStatus;
import com.arka.inventory.domain.inventorybalance.enumtype.StockMovementType;
import com.arka.inventory.domain.inventorybalance.enumtype.StockReservationStatus;
import com.arka.inventory.domain.inventorybalance.event.InventoryMutationEvent;
import com.arka.inventory.domain.inventorybalance.service.CheckoutReservationValidationService;
import com.arka.inventory.domain.inventorybalance.service.OversellGuardPolicy;
import com.arka.inventory.domain.inventorybalance.valueobject.CommitableAvailability;
import com.arka.inventory.domain.shared.event.DomainEvent;
import com.arka.inventory.domain.shared.exception.OperationNotPermittedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
public class InventoryApplicationService implements
        CreateWarehouseCommandUseCase,
        InitializeStockItemCommandUseCase,
        UpdateOperationalStockCommandUseCase,
        ReserveStockCommandUseCase,
        ConfirmReservationCommandUseCase,
        ReleaseReservationCommandUseCase,
        ExpireReservationsCommandUseCase,
        RecalculateCommitableAvailabilityCommandUseCase,
        UpdateStockItemStatusCommandUseCase,
        GetStockItemQueryUseCase,
        GetCommitableAvailabilityQueryUseCase,
        ListStockByWarehouseQueryUseCase,
        ListReservationsByCartQueryUseCase,
        GetStockMovementsQueryUseCase,
        GetLowStockQueryUseCase,
        GetInventoryAuditQueryUseCase,
        ResolveCheckoutAvailabilityQueryUseCase,
        ValidateReservationReferenceQueryUseCase {

    private static final int MAX_OPTIMISTIC_RETRIES = 3;

    private final WarehousePersistencePort warehousePersistencePort;
    private final StockItemPersistencePort stockItemPersistencePort;
    private final StockReservationPersistencePort stockReservationPersistencePort;
    private final StockMovementPersistencePort stockMovementPersistencePort;
    private final ReservationLedgerPersistencePort reservationLedgerPersistencePort;
    private final IdempotencyRecordPersistencePort idempotencyRecordPersistencePort;
    private final InventoryAuditPort inventoryAuditPort;
    private final CommitableAvailabilityCachePort commitableAvailabilityCachePort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;
    private final ActorLegitimacyPort actorLegitimacyPort;
    private final CatalogSkuPort catalogSkuPort;
    private final OrderReferencePort orderReferencePort;
    private final OrganizationDirectoryPort organizationDirectoryPort;
    private final ActorContextProviderPort actorContextProviderPort;
    private final OversellGuardPolicy oversellGuardPolicy;
    private final CheckoutReservationValidationService checkoutReservationValidationService;
    private final InventoryResultMapper resultMapper;
    private final ObjectMapper objectMapper;

    public InventoryApplicationService(
            WarehousePersistencePort warehousePersistencePort,
            StockItemPersistencePort stockItemPersistencePort,
            StockReservationPersistencePort stockReservationPersistencePort,
            StockMovementPersistencePort stockMovementPersistencePort,
            ReservationLedgerPersistencePort reservationLedgerPersistencePort,
            IdempotencyRecordPersistencePort idempotencyRecordPersistencePort,
            InventoryAuditPort inventoryAuditPort,
            CommitableAvailabilityCachePort commitableAvailabilityCachePort,
            OutboxPersistencePort outboxPersistencePort,
            ClockPort clockPort,
            ActorLegitimacyPort actorLegitimacyPort,
            CatalogSkuPort catalogSkuPort,
            OrderReferencePort orderReferencePort,
            OrganizationDirectoryPort organizationDirectoryPort,
            ActorContextProviderPort actorContextProviderPort,
            OversellGuardPolicy oversellGuardPolicy,
            CheckoutReservationValidationService checkoutReservationValidationService,
            InventoryResultMapper resultMapper,
            ObjectMapper objectMapper) {
        this.warehousePersistencePort = warehousePersistencePort;
        this.stockItemPersistencePort = stockItemPersistencePort;
        this.stockReservationPersistencePort = stockReservationPersistencePort;
        this.stockMovementPersistencePort = stockMovementPersistencePort;
        this.reservationLedgerPersistencePort = reservationLedgerPersistencePort;
        this.idempotencyRecordPersistencePort = idempotencyRecordPersistencePort;
        this.inventoryAuditPort = inventoryAuditPort;
        this.commitableAvailabilityCachePort = commitableAvailabilityCachePort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
        this.actorLegitimacyPort = actorLegitimacyPort;
        this.catalogSkuPort = catalogSkuPort;
        this.orderReferencePort = orderReferencePort;
        this.organizationDirectoryPort = organizationDirectoryPort;
        this.actorContextProviderPort = actorContextProviderPort;
        this.oversellGuardPolicy = oversellGuardPolicy;
        this.checkoutReservationValidationService = checkoutReservationValidationService;
        this.resultMapper = resultMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Mono<WarehouseResult> handle(CreateWarehouseCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String warehouseCode = normalizeRequired(command.warehouseCode(), "warehouseCode").toUpperCase(Locale.ROOT);
        String operationName = "CreateWarehouse";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadWarehouseResult(organizationId, record.resourceId()),
                        () -> warehousePersistencePort.existsByOrganizationAndCode(organizationId, warehouseCode)
                                .flatMap(exists -> exists
                                        ? Mono.error(new InventoryConflictException("Warehouse code already exists for organization"))
                                        : Mono.empty())
                                .then(Mono.defer(() -> {
                                    Instant now = now();
                                    Warehouse warehouse = new Warehouse(
                                            UUID.randomUUID().toString(),
                                            organizationId,
                                            warehouseCode,
                                            normalizeRequired(command.warehouseName(), "warehouseName"),
                                            normalizeCountryCode(command.countryCode()),
                                            null,
                                            now,
                                            now);

                                    return warehousePersistencePort.save(warehouse)
                                            .flatMap(saved -> registerMutation(
                                                            organizationId,
                                                            actorUserId,
                                                            operationName,
                                                            "Warehouse",
                                                            saved.warehouseId(),
                                                            payload("warehouseCode", saved.code(), "countryCode", saved.countryCode()),
                                                            List.of())
                                                    .thenReturn(resultMapper.toResult(saved)));
                                })),
                        WarehouseResult::warehouseId,
                        "Warehouse"));
    }

    @Override
    @Transactional
    public Mono<StockItemResult> handle(InitializeStockItemCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String warehouseId = normalizeRequired(command.warehouseId(), "warehouseId");
        String sku = normalizeSku(command.sku());
        int initialQty = requireNonNegative(command.initialPhysicalQty(), "initialPhysicalQty");
        int reorderPoint = requireNonNegative(command.reorderPoint(), "reorderPoint");
        int safetyStock = requireNonNegative(command.safetyStock(), "safetyStock");
        String operationName = "InitializeStockItem";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadStockItemResult(organizationId, record.resourceId()),
                        () -> loadWarehouse(organizationId, warehouseId)
                                .then(catalogSkuPort.existsSellableSku(organizationId, sku))
                                .flatMap(exists -> exists
                                        ? Mono.empty()
                                        : Mono.error(new InventoryValidationException("SKU is not sellable for organization")))
                                .then(stockItemPersistencePort.existsByOrganizationWarehouseSku(organizationId, warehouseId, sku))
                                .flatMap(exists -> exists
                                        ? Mono.error(new InventoryConflictException("Stock item already exists for organization + warehouse + sku"))
                                        : Mono.empty())
                                .then(Mono.defer(() -> {
                                    Instant now = now();
                                    StockItem stockItem = StockItem.initialize(
                                            UUID.randomUUID().toString(),
                                            organizationId,
                                            warehouseId,
                                            sku,
                                            initialQty,
                                            reorderPoint,
                                            safetyStock,
                                            now);

                                    return stockItemPersistencePort.insert(stockItem)
                                            .flatMap(saved -> {
                                                InventoryBalance balance = InventoryBalance.from(saved);
                                                balance.updateOperationalStock(0, "InitializeStockItem", now);
                                                StockMovement movement = stockMovement(
                                                        organizationId,
                                                        saved,
                                                        StockMovementType.INITIALIZED,
                                                        saved.physicalQty(),
                                                        "InitializeStockItem",
                                                        null,
                                                        null,
                                                        command.idempotencyKey(),
                                                        now);
                                                return stockMovementPersistencePort.save(movement)
                                                        .then(registerMutation(
                                                                organizationId,
                                                                actorUserId,
                                                                operationName,
                                                                "StockItem",
                                                                saved.stockItemId(),
                                                                payload(
                                                                        "warehouseId", saved.warehouseId(),
                                                                        "sku", saved.sku(),
                                                                        "physicalQty", saved.physicalQty()),
                                                                balance.pullDomainEvents()))
                                                        .then(refreshAvailabilityCache(saved))
                                                        .thenReturn(resultMapper.toResult(saved));
                                            });
                                })),
                        StockItemResult::stockItemId,
                        "StockItem"));
    }

    @Override
    @Transactional
    public Mono<StockItemResult> handle(UpdateOperationalStockCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String stockItemId = normalizeRequired(command.stockItemId(), "stockItemId");
        int deltaQty = requireInteger(command.deltaQty(), "deltaQty");
        String reason = normalizeRequired(command.reason(), "reason");
        String operationName = "UpdateOperationalStock";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadStockItemResult(organizationId, record.resourceId()),
                        () -> updateOperationalStockWithRetry(
                                organizationId,
                                stockItemId,
                                deltaQty,
                                reason,
                                actorUserId,
                                command.idempotencyKey(),
                                MAX_OPTIMISTIC_RETRIES),
                        StockItemResult::stockItemId,
                        "StockItem"));
    }

    @Override
    @Transactional
    public Mono<StockReservationResult> handle(ReserveStockCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String stockItemId = normalizeRequired(command.stockItemId(), "stockItemId");
        String cartId = normalizeRequired(command.cartId(), "cartId");
        int qty = requirePositive(command.qty(), "qty");
        Instant expiresAt = command.expiresAt() == null ? now().plus(15, ChronoUnit.MINUTES) : command.expiresAt();
        String operationName = "ReserveStock";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadReservationResult(organizationId, record.resourceId()),
                        () -> orderReferencePort.isValidCartReference(organizationId, cartId)
                                .flatMap(valid -> valid
                                        ? Mono.empty()
                                        : Mono.error(new InventoryValidationException("cartId is not valid for organization")))
                                .then(reserveStockWithRetry(
                                        organizationId,
                                        stockItemId,
                                        cartId,
                                        qty,
                                        expiresAt,
                                        actorUserId,
                                        command.idempotencyKey(),
                                        MAX_OPTIMISTIC_RETRIES)),
                        StockReservationResult::reservationId,
                        "StockReservation"));
    }

    @Override
    @Transactional
    public Mono<StockReservationResult> handle(ConfirmReservationCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String reservationId = normalizeRequired(command.reservationId(), "reservationId");
        String orderId = normalizeRequired(command.orderId(), "orderId");
        String operationName = "ConfirmReservation";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadReservationResult(organizationId, record.resourceId()),
                        () -> orderReferencePort.isValidOrderReference(organizationId, orderId)
                                .flatMap(valid -> valid
                                        ? Mono.empty()
                                        : Mono.error(new InventoryValidationException("orderId is not valid for organization")))
                                .then(confirmReservationWithRetry(
                                        organizationId,
                                        reservationId,
                                        orderId,
                                        actorUserId,
                                        command.idempotencyKey(),
                                        MAX_OPTIMISTIC_RETRIES)),
                        StockReservationResult::reservationId,
                        "StockReservation"));
    }

    @Override
    @Transactional
    public Mono<StockReservationResult> handle(ReleaseReservationCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String reservationId = normalizeRequired(command.reservationId(), "reservationId");
        String reason = normalizeRequired(command.reason(), "reason");
        String operationName = "ReleaseReservation";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadReservationResult(organizationId, record.resourceId()),
                        () -> releaseReservationWithRetry(
                                organizationId,
                                reservationId,
                                reason,
                                actorUserId,
                                command.idempotencyKey(),
                                MAX_OPTIMISTIC_RETRIES),
                        StockReservationResult::reservationId,
                        "StockReservation"));
    }

    @Override
    @Transactional
    public Mono<ExpiredReservationsResult> handle(ExpireReservationsCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        int batchSize = command.batchSize() == null ? 200 : requirePositive(command.batchSize(), "batchSize");

        return ensureActorAccess(organizationId, actorUserId, true)
                .thenMany(stockReservationPersistencePort.findExpiredActive(organizationId, now(), batchSize))
                .concatMap(reservation -> expireSingleReservation(organizationId, reservation, actorUserId, MAX_OPTIMISTIC_RETRIES)
                        .onErrorResume(error -> Mono.empty()))
                .count()
                .map(count -> new ExpiredReservationsResult(count.intValue()));
    }

    @Override
    @Transactional
    public Mono<CommitableAvailabilityResult> handle(RecalculateCommitableAvailabilityCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String stockItemId = normalizeRequired(command.stockItemId(), "stockItemId");
        String operationName = "RecalculateCommitableAvailability";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadStockItem(organizationId, record.resourceId())
                                .map(this::toAvailabilityResult),
                        () -> loadStockItem(organizationId, stockItemId)
                                .flatMap(stockItem -> {
                                    Instant now = now();
                                    InventoryBalance balance = InventoryBalance.from(stockItem);
                                    balance.recalculateCommitableAvailability(
                                            normalizeOptional(command.reason(), "manual-recalculation"), now);
                                    return registerMutation(
                                                    organizationId,
                                                    actorUserId,
                                                    operationName,
                                                    "StockItem",
                                                    stockItem.stockItemId(),
                                                    payload("reason", normalizeOptional(command.reason(), "manual-recalculation")),
                                                    balance.pullDomainEvents())
                                            .then(refreshAvailabilityCache(stockItem))
                                            .thenReturn(toAvailabilityResult(stockItem));
                                }),
                        result -> stockItemId,
                        "StockItem"));
    }

    @Override
    @Transactional
    public Mono<StockItemResult> handle(UpdateStockItemStatusCommand command) {
        String organizationId = normalizeRequired(command.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        String stockItemId = normalizeRequired(command.stockItemId(), "stockItemId");
        StockItemStatus targetStatus = parseStockItemStatus(command.targetStatus());
        String reason = normalizeOptional(command.reason(), "UpdateStockItemStatus");
        String operationName = "UpdateStockItemStatus";
        String requestHash = IdempotencySupport.sha256(String.valueOf(command));

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(executeIdempotent(
                        organizationId,
                        operationName,
                        command.idempotencyKey(),
                        requestHash,
                        record -> loadStockItemResult(organizationId, record.resourceId()),
                        () -> updateStockStatusWithRetry(
                                organizationId,
                                stockItemId,
                                targetStatus,
                                reason,
                                actorUserId,
                                command.idempotencyKey(),
                                MAX_OPTIMISTIC_RETRIES),
                        StockItemResult::stockItemId,
                        "StockItem"));
    }

    @Override
    public Mono<StockItemResult> handle(GetStockItemQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String stockItemId = normalizeRequired(query.stockItemId(), "stockItemId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadStockItemResult(organizationId, stockItemId));
    }

    @Override
    public Mono<CommitableAvailabilityResult> handle(GetCommitableAvailabilityQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String warehouseId = normalizeRequired(query.warehouseId(), "warehouseId");
        String sku = normalizeSku(query.sku());

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(commitableAvailabilityCachePort.find(organizationId, warehouseId, sku))
                .switchIfEmpty(stockItemPersistencePort.findByOrganizationWarehouseSku(organizationId, warehouseId, sku)
                        .switchIfEmpty(Mono.error(new InventoryResourceNotFoundException("Stock item not found for organization + warehouse + sku")))
                        .map(this::toAvailabilityResult)
                        .flatMap(result -> commitableAvailabilityCachePort.put(result).thenReturn(result)));
    }

    @Override
    public Flux<StockItemResult> handle(ListStockByWarehouseQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String warehouseId = normalizeRequired(query.warehouseId(), "warehouseId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .thenMany(stockItemPersistencePort.findByOrganizationAndWarehouse(organizationId, warehouseId)
                        .map(resultMapper::toResult));
    }

    @Override
    public Flux<StockReservationResult> handle(ListReservationsByCartQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String cartId = normalizeRequired(query.cartId(), "cartId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .thenMany(stockReservationPersistencePort.findByOrganizationAndCart(organizationId, cartId)
                        .map(resultMapper::toResult));
    }

    @Override
    public Flux<StockMovementResult> handle(GetStockMovementsQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String stockItemId = normalizeRequired(query.stockItemId(), "stockItemId");
        int limit = query.limit() == null ? 100 : requirePositive(query.limit(), "limit");

        return ensureActorAccess(organizationId, actorUserId, false)
                .thenMany(stockMovementPersistencePort.findByOrganizationAndStockItem(organizationId, stockItemId, limit)
                        .map(resultMapper::toResult));
    }

    @Override
    public Flux<StockItemResult> handle(GetLowStockQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String warehouseId = normalizeRequired(query.warehouseId(), "warehouseId");

        return ensureActorAccess(organizationId, actorUserId, false)
                .thenMany(stockItemPersistencePort.findLowStockByOrganizationAndWarehouse(organizationId, warehouseId)
                        .map(resultMapper::toResult));
    }

    @Override
    public Mono<InventoryAuditResult> handle(GetInventoryAuditQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        int limit = query.limit() == null ? 100 : requirePositive(query.limit(), "limit");

        return ensureActorAccess(organizationId, actorUserId, true)
                .then(inventoryAuditPort.findByOrganization(organizationId, limit).collectList())
                .map(entries -> new InventoryAuditResult(organizationId, entries));
    }

    @Override
    public Mono<CheckoutAvailabilityResult> handle(ResolveCheckoutAvailabilityQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String actorUserId = normalizeRequired(query.actorUserId(), "actorUserId");
        String stockItemId = normalizeRequired(query.stockItemId(), "stockItemId");
        int requestedQty = requirePositive(query.requestedQty(), "requestedQty");

        return ensureActorAccess(organizationId, actorUserId, false)
                .then(loadStockItem(organizationId, stockItemId)
                        .map(stockItem -> new CheckoutAvailabilityResult(
                                stockItem.stockItemId(),
                                requestedQty,
                                stockItem.availableQty(),
                                stockItem.status().allowsReservations() && requestedQty <= stockItem.availableQty())));
    }

    @Override
    public Mono<ReservationValidationResult> handle(ValidateReservationReferenceQuery query) {
        String organizationId = normalizeRequired(query.organizationId(), "organizationId");
        String reservationId = normalizeRequired(query.reservationId(), "reservationId");
        String sku = normalizeSku(query.sku());
        int qty = requirePositive(query.qty(), "qty");
        Instant now = now();

        return stockReservationPersistencePort.findById(organizationId, reservationId)
                .map(reservation -> {
                    boolean statusAllowsCheckout = reservation.status().isActive()
                            || reservation.status() == StockReservationStatus.CONFIRMED;
                    boolean notExpired = !reservation.isExpired(now);
                    boolean skuMatches = reservation.sku().equalsIgnoreCase(sku);
                    boolean qtyMatches = reservation.qty() == qty;
                    boolean valid = statusAllowsCheckout && notExpired && skuMatches && qtyMatches;
                    return new ReservationValidationResult(
                            reservation.reservationId(),
                            reservation.sku(),
                            reservation.qty(),
                            valid,
                            valid);
                })
                .defaultIfEmpty(new ReservationValidationResult(
                        reservationId,
                        sku,
                        qty,
                        false,
                        false));
    }

    private Mono<StockItemResult> updateOperationalStockWithRetry(
            String organizationId,
            String stockItemId,
            int deltaQty,
            String reason,
            String actorUserId,
            String correlationId,
            int retriesLeft) {
        return loadStockItem(organizationId, stockItemId)
                .flatMap(current -> {
                    Instant now = now();
                    InventoryBalance balance = InventoryBalance.from(current);
                    balance.updateOperationalStock(deltaQty, reason, now);
                    StockItem next = balance.stockItem().withIncrementedVersion(now);
                    return stockItemPersistencePort.updateWithExpectedVersion(next, current.version())
                            .flatMap(updated -> {
                                if (Boolean.TRUE.equals(updated)) {
                                    StockMovement movement = stockMovement(
                                            organizationId,
                                            next,
                                            StockMovementType.OPERATIONAL_ADJUSTMENT,
                                            deltaQty,
                                            reason,
                                            null,
                                            null,
                                            correlationId,
                                            now);
                                    return stockMovementPersistencePort.save(movement)
                                            .then(registerMutation(
                                                    organizationId,
                                                    actorUserId,
                                                    "UpdateOperationalStock",
                                                    "StockItem",
                                                    next.stockItemId(),
                                                    payload("deltaQty", deltaQty, "reason", reason),
                                                    balance.pullDomainEvents()))
                                            .then(refreshAvailabilityCache(next))
                                            .thenReturn(resultMapper.toResult(next));
                                }
                                if (retriesLeft > 0) {
                                    return updateOperationalStockWithRetry(
                                            organizationId,
                                            stockItemId,
                                            deltaQty,
                                            reason,
                                            actorUserId,
                                            correlationId,
                                            retriesLeft - 1);
                                }
                                return Mono.error(new InventoryConflictException("Concurrent stock mutation conflict"));
                            });
                });
    }

    private Mono<StockReservationResult> reserveStockWithRetry(
            String organizationId,
            String stockItemId,
            String cartId,
            int qty,
            Instant expiresAt,
            String actorUserId,
            String correlationId,
            int retriesLeft) {
        return loadStockItem(organizationId, stockItemId)
                .flatMap(current -> {
                    oversellGuardPolicy.assertReservationAllowed(current, qty);
                    Instant now = now();
                    InventoryBalance balance = InventoryBalance.from(current);
                    StockReservation reservation = balance.reserveStock(
                            UUID.randomUUID().toString(),
                            cartId,
                            qty,
                            expiresAt,
                            now);
                    StockItem next = balance.stockItem().withIncrementedVersion(now);

                    return stockItemPersistencePort.updateWithExpectedVersion(next, current.version())
                            .flatMap(updated -> {
                                if (Boolean.TRUE.equals(updated)) {
                                    StockMovement movement = stockMovement(
                                            organizationId,
                                            next,
                                            StockMovementType.RESERVATION_CREATED,
                                            0,
                                            "ReserveStock",
                                            reservation.reservationId(),
                                            null,
                                            correlationId,
                                            now);
                                    ReservationLedger ledger = reservationLedger(
                                            organizationId,
                                            reservation.reservationId(),
                                            "RESERVED",
                                            reservation.qty(),
                                            "cartId=" + cartId,
                                            now);
                                    return stockReservationPersistencePort.save(reservation)
                                            .then(stockMovementPersistencePort.save(movement))
                                            .then(reservationLedgerPersistencePort.save(ledger))
                                            .then(registerMutation(
                                                    organizationId,
                                                    actorUserId,
                                                    "ReserveStock",
                                                    "StockReservation",
                                                    reservation.reservationId(),
                                                    payload(
                                                            "stockItemId", reservation.stockItemId(),
                                                            "qty", reservation.qty(),
                                                            "cartId", reservation.cartId()),
                                                    balance.pullDomainEvents()))
                                            .then(refreshAvailabilityCache(next))
                                            .thenReturn(resultMapper.toResult(reservation));
                                }
                                if (retriesLeft > 0) {
                                    return reserveStockWithRetry(
                                            organizationId,
                                            stockItemId,
                                            cartId,
                                            qty,
                                            expiresAt,
                                            actorUserId,
                                            correlationId,
                                            retriesLeft - 1);
                                }
                                return Mono.error(new InventoryConflictException("Concurrent reservation conflict"));
                            });
                });
    }

    private Mono<StockReservationResult> confirmReservationWithRetry(
            String organizationId,
            String reservationId,
            String orderId,
            String actorUserId,
            String correlationId,
            int retriesLeft) {
        return loadReservation(organizationId, reservationId)
                .flatMap(reservation -> loadStockItem(organizationId, reservation.stockItemId())
                        .flatMap(current -> {
                            Instant now = now();
                            checkoutReservationValidationService.ensureConfirmable(reservation, now);
                            InventoryBalance balance = InventoryBalance.from(current);
                            StockReservation confirmed = balance.confirmReservation(reservation, orderId, now);
                            StockItem next = balance.stockItem().withIncrementedVersion(now);

                            return stockItemPersistencePort.updateWithExpectedVersion(next, current.version())
                                    .flatMap(updated -> {
                                        if (Boolean.TRUE.equals(updated)) {
                                            StockMovement movement = stockMovement(
                                                    organizationId,
                                                    next,
                                                    StockMovementType.RESERVATION_CONFIRMED,
                                                    -confirmed.qty(),
                                                    "ConfirmReservation",
                                                    confirmed.reservationId(),
                                                    orderId,
                                                    correlationId,
                                                    now);
                                            ReservationLedger ledger = reservationLedger(
                                                    organizationId,
                                                    confirmed.reservationId(),
                                                    "CONFIRMED",
                                                    confirmed.qty(),
                                                    "orderId=" + orderId,
                                                    now);
                                            return stockReservationPersistencePort.save(confirmed)
                                                    .then(stockMovementPersistencePort.save(movement))
                                                    .then(reservationLedgerPersistencePort.save(ledger))
                                                    .then(registerMutation(
                                                            organizationId,
                                                            actorUserId,
                                                            "ConfirmReservation",
                                                            "StockReservation",
                                                            confirmed.reservationId(),
                                                            payload("orderId", orderId, "qty", confirmed.qty()),
                                                            balance.pullDomainEvents()))
                                                    .then(refreshAvailabilityCache(next))
                                                    .thenReturn(resultMapper.toResult(confirmed));
                                        }
                                        if (retriesLeft > 0) {
                                            return confirmReservationWithRetry(
                                                    organizationId,
                                                    reservationId,
                                                    orderId,
                                                    actorUserId,
                                                    correlationId,
                                                    retriesLeft - 1);
                                        }
                                        return Mono.error(new InventoryConflictException("Concurrent confirmation conflict"));
                                    });
                        }));
    }

    private Mono<StockReservationResult> releaseReservationWithRetry(
            String organizationId,
            String reservationId,
            String reason,
            String actorUserId,
            String correlationId,
            int retriesLeft) {
        return loadReservation(organizationId, reservationId)
                .flatMap(reservation -> loadStockItem(organizationId, reservation.stockItemId())
                        .flatMap(current -> {
                            Instant now = now();
                            InventoryBalance balance = InventoryBalance.from(current);
                            StockReservation released = balance.releaseReservation(reservation, now);
                            StockItem next = balance.stockItem().withIncrementedVersion(now);

                            return stockItemPersistencePort.updateWithExpectedVersion(next, current.version())
                                    .flatMap(updated -> {
                                        if (Boolean.TRUE.equals(updated)) {
                                            StockMovement movement = stockMovement(
                                                    organizationId,
                                                    next,
                                                    StockMovementType.RESERVATION_RELEASED,
                                                    0,
                                                    reason,
                                                    released.reservationId(),
                                                    released.orderId(),
                                                    correlationId,
                                                    now);
                                            ReservationLedger ledger = reservationLedger(
                                                    organizationId,
                                                    released.reservationId(),
                                                    "RELEASED",
                                                    released.qty(),
                                                    reason,
                                                    now);
                                            return stockReservationPersistencePort.save(released)
                                                    .then(stockMovementPersistencePort.save(movement))
                                                    .then(reservationLedgerPersistencePort.save(ledger))
                                                    .then(registerMutation(
                                                            organizationId,
                                                            actorUserId,
                                                            "ReleaseReservation",
                                                            "StockReservation",
                                                            released.reservationId(),
                                                            payload("reason", reason, "qty", released.qty()),
                                                            balance.pullDomainEvents()))
                                                    .then(refreshAvailabilityCache(next))
                                                    .thenReturn(resultMapper.toResult(released));
                                        }
                                        if (retriesLeft > 0) {
                                            return releaseReservationWithRetry(
                                                    organizationId,
                                                    reservationId,
                                                    reason,
                                                    actorUserId,
                                                    correlationId,
                                                    retriesLeft - 1);
                                        }
                                        return Mono.error(new InventoryConflictException("Concurrent release conflict"));
                                    });
                        }));
    }

    private Mono<Boolean> expireSingleReservation(
            String organizationId,
            StockReservation reservation,
            String actorUserId,
            int retriesLeft) {
        return loadStockItem(organizationId, reservation.stockItemId())
                .flatMap(current -> {
                    Instant now = now();
                    InventoryBalance balance = InventoryBalance.from(current);
                    StockReservation expired = balance.expireReservation(reservation, now);
                    StockItem next = balance.stockItem().withIncrementedVersion(now);

                    return stockItemPersistencePort.updateWithExpectedVersion(next, current.version())
                            .flatMap(updated -> {
                                if (Boolean.TRUE.equals(updated)) {
                                    StockMovement movement = stockMovement(
                                            organizationId,
                                            next,
                                            StockMovementType.RESERVATION_EXPIRED,
                                            0,
                                            "ExpireReservation",
                                            expired.reservationId(),
                                            expired.orderId(),
                                            "expire-batch",
                                            now);
                                    ReservationLedger ledger = reservationLedger(
                                            organizationId,
                                            expired.reservationId(),
                                            "EXPIRED",
                                            expired.qty(),
                                            "Reservation TTL reached",
                                            now);

                                    return stockReservationPersistencePort.save(expired)
                                            .then(stockMovementPersistencePort.save(movement))
                                            .then(reservationLedgerPersistencePort.save(ledger))
                                            .then(registerMutation(
                                                    organizationId,
                                                    actorUserId,
                                                    "ExpireReservation",
                                                    "StockReservation",
                                                    expired.reservationId(),
                                                    payload("qty", expired.qty()),
                                                    balance.pullDomainEvents()))
                                            .then(refreshAvailabilityCache(next))
                                            .thenReturn(true);
                                }
                                if (retriesLeft > 0) {
                                    return expireSingleReservation(organizationId, reservation, actorUserId, retriesLeft - 1);
                                }
                                return Mono.error(new InventoryConflictException("Concurrent expiration conflict"));
                            });
                });
    }

    private Mono<StockItemResult> updateStockStatusWithRetry(
            String organizationId,
            String stockItemId,
            StockItemStatus targetStatus,
            String reason,
            String actorUserId,
            String correlationId,
            int retriesLeft) {
        return loadStockItem(organizationId, stockItemId)
                .flatMap(current -> {
                    Instant now = now();
                    InventoryBalance balance = InventoryBalance.from(current);
                    balance.markStockStatus(targetStatus, now, reason);
                    StockItem next = balance.stockItem().withIncrementedVersion(now);

                    return stockItemPersistencePort.updateWithExpectedVersion(next, current.version())
                            .flatMap(updated -> {
                                if (Boolean.TRUE.equals(updated)) {
                                    StockMovement movement = stockMovement(
                                            organizationId,
                                            next,
                                            StockMovementType.RECONCILIATION,
                                            0,
                                            reason,
                                            null,
                                            null,
                                            correlationId,
                                            now);
                                    return stockMovementPersistencePort.save(movement)
                                            .then(registerMutation(
                                                    organizationId,
                                                    actorUserId,
                                                    "UpdateStockItemStatus",
                                                    "StockItem",
                                                    next.stockItemId(),
                                                    payload("targetStatus", targetStatus.name(), "reason", reason),
                                                    balance.pullDomainEvents()))
                                            .then(refreshAvailabilityCache(next))
                                            .thenReturn(resultMapper.toResult(next));
                                }
                                if (retriesLeft > 0) {
                                    return updateStockStatusWithRetry(
                                            organizationId,
                                            stockItemId,
                                            targetStatus,
                                            reason,
                                            actorUserId,
                                            correlationId,
                                            retriesLeft - 1);
                                }
                                return Mono.error(new InventoryConflictException("Concurrent status mutation conflict"));
                            });
                });
    }

    private Mono<ActorContext> ensureActorAccess(String organizationId, String actorUserId, boolean requireAdmin) {
        return actorContextProviderPort.currentActor()
                .switchIfEmpty(Mono.error(new OperationNotPermittedException("Authenticated actor context is required")))
                .flatMap(context -> {
                    if (context.internalActor()) {
                        return Mono.just(context);
                    }
                    if (!actorUserId.equals(context.userId())) {
                        return Mono.error(new OperationNotPermittedException("Actor does not match authenticated principal"));
                    }
                    if (!organizationId.equals(context.organizationId())) {
                        return Mono.error(new OperationNotPermittedException("Organization isolation violation"));
                    }
                    if (requireAdmin && !context.inventoryAdmin()) {
                        return Mono.error(new OperationNotPermittedException("Inventory admin role is required"));
                    }
                    return actorLegitimacyPort.isLegitimate(actorUserId)
                            .flatMap(legitimate -> legitimate
                                    ? Mono.just(context)
                                    : Mono.error(new OperationNotPermittedException("Actor is not legitimate")));
                })
                .flatMap(context -> organizationDirectoryPort.organizationExists(organizationId)
                        .flatMap(exists -> exists
                                ? Mono.just(context)
                                : Mono.error(new InventoryValidationException("Unknown organizationId"))));
    }

    private <T> Mono<T> executeIdempotent(
            String organizationId,
            String operationName,
            String idempotencyKey,
            String requestHash,
            Function<IdempotencyRecord, Mono<T>> replayLoader,
            Supplier<Mono<T>> operation,
            Function<T, String> resourceIdExtractor,
            String resourceType) {
        String normalizedIdempotencyKey = IdempotencySupport.normalizeKey(idempotencyKey);

        return idempotencyRecordPersistencePort
                .findByOrganizationOperationAndKey(organizationId, operationName, normalizedIdempotencyKey)
                .flatMap(existing -> {
                    if (!existing.requestHash().equals(requestHash)) {
                        return Mono.error(new IdempotencyConflictException(
                                "Idempotency key was already used with a different request payload"));
                    }
                    return replayLoader.apply(existing);
                })
                .switchIfEmpty(Mono.defer(() -> operation.get()
                        .flatMap(result -> {
                            IdempotencyRecord record = new IdempotencyRecord(
                                    UUID.randomUUID().toString(),
                                    organizationId,
                                    operationName,
                                    normalizedIdempotencyKey,
                                    requestHash,
                                    resourceType,
                                    resourceIdExtractor.apply(result),
                                    200,
                                    now(),
                                    now());
                            return idempotencyRecordPersistencePort.save(record).thenReturn(result);
                        })));
    }

    private Mono<Warehouse> loadWarehouse(String organizationId, String warehouseId) {
        return warehousePersistencePort.findById(organizationId, warehouseId)
                .switchIfEmpty(Mono.error(new InventoryResourceNotFoundException("Warehouse not found")));
    }

    private Mono<StockItem> loadStockItem(String organizationId, String stockItemId) {
        return stockItemPersistencePort.findById(organizationId, stockItemId)
                .switchIfEmpty(Mono.error(new InventoryResourceNotFoundException("Stock item not found")));
    }

    private Mono<StockReservation> loadReservation(String organizationId, String reservationId) {
        return stockReservationPersistencePort.findById(organizationId, reservationId)
                .switchIfEmpty(Mono.error(new InventoryResourceNotFoundException("Stock reservation not found")));
    }

    private Mono<WarehouseResult> loadWarehouseResult(String organizationId, String warehouseId) {
        return loadWarehouse(organizationId, warehouseId).map(resultMapper::toResult);
    }

    private Mono<StockItemResult> loadStockItemResult(String organizationId, String stockItemId) {
        return loadStockItem(organizationId, stockItemId).map(resultMapper::toResult);
    }

    private Mono<StockReservationResult> loadReservationResult(String organizationId, String reservationId) {
        return loadReservation(organizationId, reservationId).map(resultMapper::toResult);
    }

    private Mono<Void> registerMutation(
            String organizationId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String payload,
            List<? extends DomainEvent> domainEvents) {
        List<DomainEvent> eventsToPublish = new ArrayList<>();
        if (domainEvents != null) {
            eventsToPublish.addAll(domainEvents);
        }
        eventsToPublish.add(new InventoryMutationEvent(
                now(),
                targetId,
                organizationId,
                actionType,
                targetType,
                targetId,
                actorUserId));

        return inventoryAuditPort
                .record(
                        organizationId,
                        actorUserId,
                        actionType,
                        targetType,
                        targetId,
                        "SUCCESS",
                        payload)
                .then(outboxPersistencePort.storeAll(eventsToPublish));
    }

    private Mono<Void> refreshAvailabilityCache(StockItem stockItem) {
        CommitableAvailabilityResult availabilityResult = toAvailabilityResult(stockItem);
        return commitableAvailabilityCachePort.put(availabilityResult).onErrorResume(error -> Mono.empty());
    }

    private CommitableAvailabilityResult toAvailabilityResult(StockItem stockItem) {
        CommitableAvailability availability = CommitableAvailability.from(
                stockItem.organizationId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                stockItem.physicalQty(),
                stockItem.reservedQty(),
                stockItem.reorderPoint(),
                stockItem.safetyStock());
        return resultMapper.toResult(availability);
    }

    private StockMovement stockMovement(
            String organizationId,
            StockItem stockItem,
            StockMovementType movementType,
            int deltaQty,
            String reason,
            String reservationId,
            String orderId,
            String correlationId,
            Instant now) {
        return new StockMovement(
                UUID.randomUUID().toString(),
                organizationId,
                stockItem.stockItemId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                movementType,
                deltaQty,
                normalizeRequired(reason, "reason"),
                reservationId,
                orderId,
                correlationId,
                now);
    }

    private ReservationLedger reservationLedger(
            String organizationId,
            String reservationId,
            String entryType,
            int qty,
            String note,
            Instant now) {
        return new ReservationLedger(
                UUID.randomUUID().toString(),
                organizationId,
                reservationId,
                normalizeRequired(entryType, "entryType"),
                qty,
                note,
                now);
    }

    private String payload(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return "{}";
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = (i + 1) < keyValues.length ? keyValues[i + 1] : null;
            payload.put(key, value);
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize payload", exception);
        }
    }

    private StockItemStatus parseStockItemStatus(String rawStatus) {
        String normalized = normalizeRequired(rawStatus, "targetStatus").toUpperCase(Locale.ROOT);
        try {
            return StockItemStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new InventoryValidationException("Invalid stock status: " + rawStatus);
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InventoryValidationException(fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeOptional(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String normalizeCountryCode(String countryCode) {
        String normalized = normalizeRequired(countryCode, "countryCode").toUpperCase(Locale.ROOT);
        if (normalized.length() != 2) {
            throw new InventoryValidationException("countryCode must be an ISO-3166 alpha-2 code");
        }
        return normalized;
    }

    private String normalizeSku(String sku) {
        return normalizeRequired(sku, "sku").toUpperCase(Locale.ROOT);
    }

    private int requirePositive(Integer value, String fieldName) {
        int parsed = requireInteger(value, fieldName);
        if (parsed <= 0) {
            throw new InventoryValidationException(fieldName + " must be positive");
        }
        return parsed;
    }

    private int requireNonNegative(Integer value, String fieldName) {
        int parsed = requireInteger(value, fieldName);
        if (parsed < 0) {
            throw new InventoryValidationException(fieldName + " must be non-negative");
        }
        return parsed;
    }

    private int requireInteger(Integer value, String fieldName) {
        if (value == null) {
            throw new InventoryValidationException(fieldName + " is required");
        }
        return value;
    }

    private Instant now() {
        return clockPort.now();
    }
}
