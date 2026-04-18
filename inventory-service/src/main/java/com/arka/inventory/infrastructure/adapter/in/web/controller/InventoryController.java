package com.arka.inventory.infrastructure.adapter.in.web.controller;

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
import com.arka.inventory.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.inventory.infrastructure.adapter.in.web.mapper.command.InventoryCommandMapper;
import com.arka.inventory.infrastructure.adapter.in.web.mapper.query.InventoryQueryMapper;
import com.arka.inventory.infrastructure.adapter.in.web.mapper.response.InventoryResponseMapper;
import com.arka.inventory.infrastructure.adapter.in.web.request.ConfirmReservationRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.CreateWarehouseRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.ExpireReservationsRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.InitializeStockItemRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.RecalculateCommitableAvailabilityRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.ReleaseReservationRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.ReserveStockRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.UpdateOperationalStockRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.UpdateStockItemStatusRequest;
import com.arka.inventory.infrastructure.adapter.in.web.response.CheckoutAvailabilityResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.CommitableAvailabilityResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.ExpiredReservationsResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.InventoryAuditResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.StockItemResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.StockMovementResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.StockReservationResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.WarehouseResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.ReservationValidationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class InventoryController {

    private final InventoryCommandMapper commandMapper;
    private final InventoryQueryMapper queryMapper;
    private final InventoryResponseMapper responseMapper;
    private final CreateWarehouseCommandUseCase createWarehouseCommandUseCase;
    private final InitializeStockItemCommandUseCase initializeStockItemCommandUseCase;
    private final UpdateOperationalStockCommandUseCase updateOperationalStockCommandUseCase;
    private final ReserveStockCommandUseCase reserveStockCommandUseCase;
    private final ConfirmReservationCommandUseCase confirmReservationCommandUseCase;
    private final ReleaseReservationCommandUseCase releaseReservationCommandUseCase;
    private final ExpireReservationsCommandUseCase expireReservationsCommandUseCase;
    private final RecalculateCommitableAvailabilityCommandUseCase recalculateCommitableAvailabilityCommandUseCase;
    private final UpdateStockItemStatusCommandUseCase updateStockItemStatusCommandUseCase;
    private final GetStockItemQueryUseCase getStockItemQueryUseCase;
    private final GetCommitableAvailabilityQueryUseCase getCommitableAvailabilityQueryUseCase;
    private final ListStockByWarehouseQueryUseCase listStockByWarehouseQueryUseCase;
    private final ListReservationsByCartQueryUseCase listReservationsByCartQueryUseCase;
    private final GetStockMovementsQueryUseCase getStockMovementsQueryUseCase;
    private final GetLowStockQueryUseCase getLowStockQueryUseCase;
    private final GetInventoryAuditQueryUseCase getInventoryAuditQueryUseCase;
    private final ResolveCheckoutAvailabilityQueryUseCase resolveCheckoutAvailabilityQueryUseCase;
    private final ValidateReservationReferenceQueryUseCase validateReservationReferenceQueryUseCase;

    public InventoryController(
            InventoryCommandMapper commandMapper,
            InventoryQueryMapper queryMapper,
            InventoryResponseMapper responseMapper,
            CreateWarehouseCommandUseCase createWarehouseCommandUseCase,
            InitializeStockItemCommandUseCase initializeStockItemCommandUseCase,
            UpdateOperationalStockCommandUseCase updateOperationalStockCommandUseCase,
            ReserveStockCommandUseCase reserveStockCommandUseCase,
            ConfirmReservationCommandUseCase confirmReservationCommandUseCase,
            ReleaseReservationCommandUseCase releaseReservationCommandUseCase,
            ExpireReservationsCommandUseCase expireReservationsCommandUseCase,
            RecalculateCommitableAvailabilityCommandUseCase recalculateCommitableAvailabilityCommandUseCase,
            UpdateStockItemStatusCommandUseCase updateStockItemStatusCommandUseCase,
            GetStockItemQueryUseCase getStockItemQueryUseCase,
            GetCommitableAvailabilityQueryUseCase getCommitableAvailabilityQueryUseCase,
            ListStockByWarehouseQueryUseCase listStockByWarehouseQueryUseCase,
            ListReservationsByCartQueryUseCase listReservationsByCartQueryUseCase,
            GetStockMovementsQueryUseCase getStockMovementsQueryUseCase,
            GetLowStockQueryUseCase getLowStockQueryUseCase,
            GetInventoryAuditQueryUseCase getInventoryAuditQueryUseCase,
            ResolveCheckoutAvailabilityQueryUseCase resolveCheckoutAvailabilityQueryUseCase,
            ValidateReservationReferenceQueryUseCase validateReservationReferenceQueryUseCase) {
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.responseMapper = responseMapper;
        this.createWarehouseCommandUseCase = createWarehouseCommandUseCase;
        this.initializeStockItemCommandUseCase = initializeStockItemCommandUseCase;
        this.updateOperationalStockCommandUseCase = updateOperationalStockCommandUseCase;
        this.reserveStockCommandUseCase = reserveStockCommandUseCase;
        this.confirmReservationCommandUseCase = confirmReservationCommandUseCase;
        this.releaseReservationCommandUseCase = releaseReservationCommandUseCase;
        this.expireReservationsCommandUseCase = expireReservationsCommandUseCase;
        this.recalculateCommitableAvailabilityCommandUseCase = recalculateCommitableAvailabilityCommandUseCase;
        this.updateStockItemStatusCommandUseCase = updateStockItemStatusCommandUseCase;
        this.getStockItemQueryUseCase = getStockItemQueryUseCase;
        this.getCommitableAvailabilityQueryUseCase = getCommitableAvailabilityQueryUseCase;
        this.listStockByWarehouseQueryUseCase = listStockByWarehouseQueryUseCase;
        this.listReservationsByCartQueryUseCase = listReservationsByCartQueryUseCase;
        this.getStockMovementsQueryUseCase = getStockMovementsQueryUseCase;
        this.getLowStockQueryUseCase = getLowStockQueryUseCase;
        this.getInventoryAuditQueryUseCase = getInventoryAuditQueryUseCase;
        this.resolveCheckoutAvailabilityQueryUseCase = resolveCheckoutAvailabilityQueryUseCase;
        this.validateReservationReferenceQueryUseCase = validateReservationReferenceQueryUseCase;
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/warehouses")
    public Mono<WarehouseResponse> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return createWarehouseCommandUseCase
                .handle(commandMapper.toCommand(request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/stock-items")
    public Mono<StockItemResponse> initializeStockItem(
            @Valid @RequestBody InitializeStockItemRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return initializeStockItemCommandUseCase
                .handle(commandMapper.toCommand(request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/stock-items/{stockItemId}/stock-adjustments")
    public Mono<StockItemResponse> updateOperationalStock(
            @PathVariable String stockItemId,
            @Valid @RequestBody UpdateOperationalStockRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateOperationalStockCommandUseCase
                .handle(commandMapper.toCommand(stockItemId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/stock-items/{stockItemId}/reservations")
    public Mono<StockReservationResponse> reserveStock(
            @PathVariable String stockItemId,
            @Valid @RequestBody ReserveStockRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return reserveStockCommandUseCase
                .handle(commandMapper.toCommand(stockItemId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/reservations/{reservationId}/confirm")
    public Mono<StockReservationResponse> confirmReservation(
            @PathVariable String reservationId,
            @Valid @RequestBody ConfirmReservationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return confirmReservationCommandUseCase
                .handle(commandMapper.toCommand(reservationId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/reservations/{reservationId}/release")
    public Mono<StockReservationResponse> releaseReservation(
            @PathVariable String reservationId,
            @Valid @RequestBody ReleaseReservationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return releaseReservationCommandUseCase
                .handle(commandMapper.toCommand(reservationId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/reservations/expire")
    public Mono<ExpiredReservationsResponse> expireReservations(
            @RequestBody(required = false) ExpireReservationsRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return expireReservationsCommandUseCase
                .handle(commandMapper.toCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/stock-items/{stockItemId}/availability/recalculate")
    public Mono<CommitableAvailabilityResponse> recalculateAvailability(
            @PathVariable String stockItemId,
            @RequestBody(required = false) RecalculateCommitableAvailabilityRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return recalculateCommitableAvailabilityCommandUseCase
                .handle(commandMapper.toCommand(stockItemId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.write', 'ROLE_INVENTORY_ADMIN')")
    @PostMapping("/stock-items/{stockItemId}/status")
    public Mono<StockItemResponse> updateStockItemStatus(
            @PathVariable String stockItemId,
            @Valid @RequestBody UpdateStockItemStatusRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateStockItemStatusCommandUseCase
                .handle(commandMapper.toCommand(stockItemId, request, principal, idempotencyKey))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/stock-items/{stockItemId}")
    public Mono<StockItemResponse> getStockItem(
            @PathVariable String stockItemId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getStockItemQueryUseCase
                .handle(queryMapper.toQuery(stockItemId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/availability")
    public Mono<CommitableAvailabilityResponse> getCommitableAvailability(
            @RequestParam String warehouseId,
            @RequestParam String sku,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getCommitableAvailabilityQueryUseCase
                .handle(queryMapper.toQuery(warehouseId, sku, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/stock-items")
    public Flux<StockItemResponse> listStockByWarehouse(
            @RequestParam String warehouseId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listStockByWarehouseQueryUseCase
                .handle(queryMapper.toListStockQuery(warehouseId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/reservations")
    public Flux<StockReservationResponse> listReservationsByCart(
            @RequestParam String cartId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listReservationsByCartQueryUseCase
                .handle(queryMapper.toListReservationsByCartQuery(cartId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/stock-items/{stockItemId}/movements")
    public Flux<StockMovementResponse> getStockMovements(
            @PathVariable String stockItemId,
            @RequestParam(defaultValue = "100") @Min(1) Integer limit,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getStockMovementsQueryUseCase
                .handle(queryMapper.toStockMovementsQuery(stockItemId, limit, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/stock-items/low-stock")
    public Flux<StockItemResponse> getLowStock(
            @RequestParam String warehouseId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getLowStockQueryUseCase
                .handle(queryMapper.toLowStockQuery(warehouseId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/audits")
    public Mono<InventoryAuditResponse> getAudits(
            @RequestParam(defaultValue = "100") @Min(1) Integer limit,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getInventoryAuditQueryUseCase
                .handle(queryMapper.toAuditQuery(limit, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('inventory.read', 'ROLE_INVENTORY_ADMIN')")
    @GetMapping("/checkout/availability")
    public Mono<CheckoutAvailabilityResponse> resolveCheckoutAvailability(
            @RequestParam String stockItemId,
            @RequestParam @Min(1) Integer requestedQty,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return resolveCheckoutAvailabilityQueryUseCase
                .handle(queryMapper.toCheckoutAvailabilityQuery(stockItemId, requestedQty, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasRole('TRUSTED_SERVICE') and hasAuthority('inventory.read')")
    @GetMapping("/internal/reservations/{reservationId}/validation")
    public Mono<ReservationValidationResponse> validateReservationReference(
            @PathVariable String reservationId,
            @RequestParam(name = "organizationId", required = false) String organizationId,
            @RequestParam String sku,
            @RequestParam @Min(1) Integer qty) {
        return validateReservationReferenceQueryUseCase
                .handle(queryMapper.toReservationValidationQuery(normalizeOrganizationId(organizationId), reservationId, sku, qty))
                .map(responseMapper::toResponse);
    }

    private String normalizeOrganizationId(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return null;
        }
        return organizationId.trim();
    }
}
