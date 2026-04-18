package com.arka.inventory.infrastructure.adapter.in.web.mapper.response;

import com.arka.inventory.application.result.CheckoutAvailabilityResult;
import com.arka.inventory.application.result.CommitableAvailabilityResult;
import com.arka.inventory.application.result.ExpiredReservationsResult;
import com.arka.inventory.application.result.InventoryAuditEntryResult;
import com.arka.inventory.application.result.InventoryAuditResult;
import com.arka.inventory.application.result.ReservationValidationResult;
import com.arka.inventory.application.result.StockItemResult;
import com.arka.inventory.application.result.StockMovementResult;
import com.arka.inventory.application.result.StockReservationResult;
import com.arka.inventory.application.result.WarehouseResult;
import com.arka.inventory.infrastructure.adapter.in.web.response.CheckoutAvailabilityResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.CommitableAvailabilityResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.ExpiredReservationsResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.InventoryAuditEntryResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.InventoryAuditResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.ReservationValidationResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.StockItemResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.StockMovementResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.StockReservationResponse;
import com.arka.inventory.infrastructure.adapter.in.web.response.WarehouseResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryResponseMapper {

    public WarehouseResponse toResponse(WarehouseResult result) {
        return new WarehouseResponse(
                result.warehouseId(),
                result.tenantId(),
                result.warehouseCode(),
                result.warehouseName(),
                result.countryCode(),
                result.status(),
                result.createdAt(),
                result.updatedAt());
    }

    public StockItemResponse toResponse(StockItemResult result) {
        return new StockItemResponse(
                result.stockItemId(),
                result.tenantId(),
                result.warehouseId(),
                result.sku(),
                result.physicalQty(),
                result.reservedQty(),
                result.availableQty(),
                result.reorderPoint(),
                result.safetyStock(),
                result.lowStock(),
                result.status(),
                result.version(),
                result.createdAt(),
                result.updatedAt());
    }

    public StockReservationResponse toResponse(StockReservationResult result) {
        return new StockReservationResponse(
                result.reservationId(),
                result.tenantId(),
                result.stockItemId(),
                result.warehouseId(),
                result.sku(),
                result.cartId(),
                result.orderId(),
                result.qty(),
                result.status(),
                result.expiresAt(),
                result.confirmedAt(),
                result.releasedAt(),
                result.createdAt(),
                result.updatedAt());
    }

    public CommitableAvailabilityResponse toResponse(CommitableAvailabilityResult result) {
        return new CommitableAvailabilityResponse(
                result.tenantId(),
                result.warehouseId(),
                result.sku(),
                result.physicalQty(),
                result.reservedQty(),
                result.availableQty(),
                result.reorderPoint(),
                result.safetyStock(),
                result.lowStock());
    }

    public StockMovementResponse toResponse(StockMovementResult result) {
        return new StockMovementResponse(
                result.movementId(),
                result.tenantId(),
                result.stockItemId(),
                result.warehouseId(),
                result.sku(),
                result.movementType(),
                result.deltaQty(),
                result.reason(),
                result.reservationId(),
                result.orderId(),
                result.correlationId(),
                result.createdAt());
    }

    public CheckoutAvailabilityResponse toResponse(CheckoutAvailabilityResult result) {
        return new CheckoutAvailabilityResponse(
                result.stockItemId(),
                result.requestedQty(),
                result.availableQty(),
                result.reservable());
    }

    public ReservationValidationResponse toResponse(ReservationValidationResult result) {
        return new ReservationValidationResponse(
                result.reservationId(),
                result.sku(),
                result.qty(),
                result.reservationConfirmed(),
                result.commitableAvailable());
    }

    public ExpiredReservationsResponse toResponse(ExpiredReservationsResult result) {
        return new ExpiredReservationsResponse(result.expiredCount());
    }

    public InventoryAuditResponse toResponse(InventoryAuditResult result) {
        return new InventoryAuditResponse(
                result.tenantId(),
                result.entries().stream().map(this::toResponse).toList());
    }

    public InventoryAuditEntryResponse toResponse(InventoryAuditEntryResult result) {
        return new InventoryAuditEntryResponse(
                result.auditId(),
                result.tenantId(),
                result.actorUserId(),
                result.actionType(),
                result.targetType(),
                result.targetId(),
                result.outcome(),
                result.payload(),
                result.createdAt());
    }
}
