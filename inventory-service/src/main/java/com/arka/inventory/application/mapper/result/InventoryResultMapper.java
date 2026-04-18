package com.arka.inventory.application.mapper.result;

import com.arka.inventory.application.result.CommitableAvailabilityResult;
import com.arka.inventory.application.result.StockItemResult;
import com.arka.inventory.application.result.StockMovementResult;
import com.arka.inventory.application.result.StockReservationResult;
import com.arka.inventory.application.result.WarehouseResult;
import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.domain.inventorybalance.entity.StockMovement;
import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.domain.inventorybalance.entity.Warehouse;
import com.arka.inventory.domain.inventorybalance.valueobject.CommitableAvailability;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultMapper {

    public WarehouseResult toResult(Warehouse warehouse) {
        return new WarehouseResult(
                warehouse.warehouseId(),
                warehouse.tenantId(),
                warehouse.code(),
                warehouse.name(),
                warehouse.countryCode(),
                warehouse.status().name(),
                warehouse.createdAt(),
                warehouse.updatedAt());
    }

    public StockItemResult toResult(StockItem stockItem) {
        return new StockItemResult(
                stockItem.stockItemId(),
                stockItem.tenantId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                stockItem.physicalQty(),
                stockItem.reservedQty(),
                stockItem.availableQty(),
                stockItem.reorderPoint(),
                stockItem.safetyStock(),
                stockItem.lowStock(),
                stockItem.status().name(),
                stockItem.version(),
                stockItem.createdAt(),
                stockItem.updatedAt());
    }

    public StockReservationResult toResult(StockReservation reservation) {
        return new StockReservationResult(
                reservation.reservationId(),
                reservation.tenantId(),
                reservation.stockItemId(),
                reservation.warehouseId(),
                reservation.sku(),
                reservation.cartId(),
                reservation.orderId(),
                reservation.qty(),
                reservation.status().name(),
                reservation.expiresAt(),
                reservation.confirmedAt(),
                reservation.releasedAt(),
                reservation.createdAt(),
                reservation.updatedAt());
    }

    public CommitableAvailabilityResult toResult(CommitableAvailability availability) {
        return new CommitableAvailabilityResult(
                availability.tenantId(),
                availability.warehouseId(),
                availability.sku(),
                availability.physicalQty(),
                availability.reservedQty(),
                availability.availableQty(),
                availability.reorderPoint(),
                availability.safetyStock(),
                availability.lowStock());
    }

    public StockMovementResult toResult(StockMovement movement) {
        return new StockMovementResult(
                movement.movementId(),
                movement.tenantId(),
                movement.stockItemId(),
                movement.warehouseId(),
                movement.sku(),
                movement.movementType().name(),
                movement.deltaQty(),
                movement.reason(),
                movement.reservationId(),
                movement.orderId(),
                movement.correlationId(),
                movement.createdAt());
    }
}
