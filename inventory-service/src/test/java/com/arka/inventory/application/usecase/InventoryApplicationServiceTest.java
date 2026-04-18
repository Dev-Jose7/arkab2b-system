package com.arka.inventory.application.usecase;

import com.arka.inventory.application.command.CreateWarehouseCommand;
import com.arka.inventory.application.command.ConfirmReservationCommand;
import com.arka.inventory.application.command.ReserveStockCommand;
import com.arka.inventory.application.mapper.result.InventoryResultMapper;
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
import com.arka.inventory.application.result.WarehouseResult;
import com.arka.inventory.application.service.InventoryApplicationService;
import com.arka.inventory.domain.inventorybalance.entity.IdempotencyRecord;
import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.domain.inventorybalance.entity.Warehouse;
import com.arka.inventory.domain.inventorybalance.enumtype.StockReservationStatus;
import com.arka.inventory.domain.inventorybalance.exception.InsufficientAvailabilityException;
import com.arka.inventory.domain.inventorybalance.exception.ReservationNotActiveException;
import com.arka.inventory.domain.inventorybalance.service.CheckoutReservationValidationService;
import com.arka.inventory.domain.inventorybalance.service.OversellGuardPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryApplicationServiceTest {

    @Mock
    private WarehousePersistencePort warehousePersistencePort;
    @Mock
    private StockItemPersistencePort stockItemPersistencePort;
    @Mock
    private StockReservationPersistencePort stockReservationPersistencePort;
    @Mock
    private StockMovementPersistencePort stockMovementPersistencePort;
    @Mock
    private ReservationLedgerPersistencePort reservationLedgerPersistencePort;
    @Mock
    private IdempotencyRecordPersistencePort idempotencyRecordPersistencePort;
    @Mock
    private InventoryAuditPort inventoryAuditPort;
    @Mock
    private CommitableAvailabilityCachePort commitableAvailabilityCachePort;
    @Mock
    private OutboxPersistencePort outboxPersistencePort;
    @Mock
    private ClockPort clockPort;
    @Mock
    private ActorLegitimacyPort actorLegitimacyPort;
    @Mock
    private CatalogSkuPort catalogSkuPort;
    @Mock
    private OrderReferencePort orderReferencePort;
    @Mock
    private OrganizationDirectoryPort organizationDirectoryPort;
    @Mock
    private ActorContextProviderPort actorContextProviderPort;

    private InventoryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new InventoryApplicationService(
                warehousePersistencePort,
                stockItemPersistencePort,
                stockReservationPersistencePort,
                stockMovementPersistencePort,
                reservationLedgerPersistencePort,
                idempotencyRecordPersistencePort,
                inventoryAuditPort,
                commitableAvailabilityCachePort,
                outboxPersistencePort,
                clockPort,
                actorLegitimacyPort,
                catalogSkuPort,
                orderReferencePort,
                organizationDirectoryPort,
                actorContextProviderPort,
                new OversellGuardPolicy(),
                new CheckoutReservationValidationService(),
                new InventoryResultMapper(),
                new ObjectMapper());

        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("actor-1", "organization-1", true, false)));
        when(actorLegitimacyPort.isLegitimate("actor-1")).thenReturn(Mono.just(true));
        when(organizationDirectoryPort.organizationExists("organization-1")).thenReturn(Mono.just(true));
        when(outboxPersistencePort.storeAll(any())).thenReturn(Mono.empty());
        when(inventoryAuditPort.record(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(commitableAvailabilityCachePort.put(any())).thenReturn(Mono.empty());
    }

    @Test
    void reserveStockRejectsWhenRequestedQtyExceedsAvailability() {
        when(orderReferencePort.isValidCartReference("organization-1", "cart-1")).thenReturn(Mono.just(true));
        when(idempotencyRecordPersistencePort.findByOrganizationOperationAndKey("organization-1", "ReserveStock", "idem-1"))
                .thenReturn(Mono.empty());
        when(stockItemPersistencePort.findById("organization-1", "stock-1"))
                .thenReturn(Mono.just(stockItemWith(5, 4)));

        ReserveStockCommand command = new ReserveStockCommand(
                "organization-1",
                "stock-1",
                "cart-1",
                2,
                Instant.parse("2026-01-01T01:00:00Z"),
                "actor-1",
                "idem-1");

        StepVerifier.create(service.handle(command))
                .expectError(InsufficientAvailabilityException.class)
                .verify();

        verify(stockReservationPersistencePort, never()).save(any());
    }

    @Test
    void confirmReservationRequiresReservationStillActive() {
        when(orderReferencePort.isValidOrderReference("organization-1", "order-1")).thenReturn(Mono.just(true));
        when(idempotencyRecordPersistencePort.findByOrganizationOperationAndKey("organization-1", "ConfirmReservation", "idem-2"))
                .thenReturn(Mono.empty());
        when(stockReservationPersistencePort.findById("organization-1", "res-1"))
                .thenReturn(Mono.just(expiredActiveReservation()));
        when(stockItemPersistencePort.findById("organization-1", "stock-1"))
                .thenReturn(Mono.just(stockItemWith(10, 3)));

        ConfirmReservationCommand command = new ConfirmReservationCommand(
                "organization-1",
                "res-1",
                "order-1",
                "actor-1",
                "idem-2");

        StepVerifier.create(service.handle(command))
                .expectError(ReservationNotActiveException.class)
                .verify();

        verify(stockItemPersistencePort, never()).updateWithExpectedVersion(any(), any(Long.class));
    }

    @Test
    void createWarehouseReplaysFromIdempotencyRecord() {
        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "organization-1",
                "wh-main",
                "Main Warehouse",
                "CO",
                "actor-1",
                "idem-3");
        String requestHash = com.arka.inventory.application.mapper.command.IdempotencySupport
                .sha256(String.valueOf(command));

        IdempotencyRecord existing = new IdempotencyRecord(
                "idem-record-1",
                "organization-1",
                "CreateWarehouse",
                "idem-3",
                requestHash,
                "Warehouse",
                "wh-1",
                200,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));

        when(idempotencyRecordPersistencePort.findByOrganizationOperationAndKey("organization-1", "CreateWarehouse", "idem-3"))
                .thenReturn(Mono.just(existing));
        when(warehousePersistencePort.findById("organization-1", "wh-1"))
                .thenReturn(Mono.just(new Warehouse(
                        "wh-1",
                        "organization-1",
                        "WH-MAIN",
                        "Main Warehouse",
                        "CO",
                        null,
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-01T00:00:00Z"))));

        StepVerifier.create(service.handle(command))
                .assertNext(result -> {
                    WarehouseResult warehouse = result;
                    org.junit.jupiter.api.Assertions.assertEquals("wh-1", warehouse.warehouseId());
                })
                .verifyComplete();

        verify(warehousePersistencePort, never()).existsByOrganizationAndCode(anyString(), anyString());
        verify(idempotencyRecordPersistencePort, never()).save(any());
    }

    @Test
    void reserveStockRetriesWhenOptimisticLockConflictsOnce() {
        when(orderReferencePort.isValidCartReference("organization-1", "cart-1")).thenReturn(Mono.just(true));
        when(idempotencyRecordPersistencePort.findByOrganizationOperationAndKey("organization-1", "ReserveStock", "idem-4"))
                .thenReturn(Mono.empty());
        when(stockItemPersistencePort.findById("organization-1", "stock-1"))
                .thenReturn(Mono.just(stockItemWith(10, 1)));
        when(stockItemPersistencePort.updateWithExpectedVersion(any(), any(Long.class)))
                .thenReturn(Mono.just(false), Mono.just(true));
        when(stockReservationPersistencePort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(stockMovementPersistencePort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(reservationLedgerPersistencePort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(idempotencyRecordPersistencePort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        ReserveStockCommand command = new ReserveStockCommand(
                "organization-1",
                "stock-1",
                "cart-1",
                2,
                Instant.parse("2026-01-01T01:00:00Z"),
                "actor-1",
                "idem-4");

        StepVerifier.create(service.handle(command))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("organization-1", result.organizationId()))
                .verifyComplete();

        verify(stockItemPersistencePort, times(2)).updateWithExpectedVersion(any(), any(Long.class));
    }

    private StockItem stockItemWith(int physicalQty, int reservedQty) {
        return new StockItem(
                "stock-1",
                "organization-1",
                "wh-1",
                "SKU-1",
                physicalQty,
                reservedQty,
                2,
                1,
                null,
                3,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private StockReservation expiredActiveReservation() {
        return new StockReservation(
                "res-1",
                "organization-1",
                "stock-1",
                "wh-1",
                "SKU-1",
                "cart-1",
                null,
                3,
                StockReservationStatus.ACTIVE,
                Instant.parse("2025-12-31T23:59:00Z"),
                null,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }
}
