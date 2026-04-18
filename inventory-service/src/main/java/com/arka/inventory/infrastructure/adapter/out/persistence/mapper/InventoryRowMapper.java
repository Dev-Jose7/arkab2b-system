package com.arka.inventory.infrastructure.adapter.out.persistence.mapper;

import com.arka.inventory.domain.inventorybalance.entity.IdempotencyRecord;
import com.arka.inventory.domain.inventorybalance.entity.InventoryAudit;
import com.arka.inventory.domain.inventorybalance.entity.ReservationLedger;
import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.domain.inventorybalance.entity.StockMovement;
import com.arka.inventory.domain.inventorybalance.entity.StockReservation;
import com.arka.inventory.domain.inventorybalance.entity.Warehouse;
import com.arka.inventory.domain.inventorybalance.enumtype.StockItemStatus;
import com.arka.inventory.domain.inventorybalance.enumtype.StockMovementType;
import com.arka.inventory.domain.inventorybalance.enumtype.StockReservationStatus;
import com.arka.inventory.domain.inventorybalance.enumtype.WarehouseStatus;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.IdempotencyRecordRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.InventoryAuditRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.ReservationLedgerRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.StockItemRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.StockMovementRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.StockReservationRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.WarehouseRow;
import org.springframework.stereotype.Component;

@Component
public class InventoryRowMapper {

    public Warehouse toDomain(WarehouseRow row) {
        return new Warehouse(
                row.warehouseId(),
                row.tenantId(),
                row.warehouseCode(),
                row.warehouseName(),
                row.countryCode(),
                WarehouseStatus.valueOf(row.status()),
                row.createdAt(),
                row.updatedAt());
    }

    public WarehouseRow toRow(Warehouse warehouse) {
        return new WarehouseRow(
                warehouse.warehouseId(),
                warehouse.tenantId(),
                warehouse.code(),
                warehouse.name(),
                warehouse.countryCode(),
                warehouse.status().name(),
                warehouse.createdAt(),
                warehouse.updatedAt());
    }

    public StockItem toDomain(StockItemRow row) {
        return new StockItem(
                row.stockItemId(),
                row.tenantId(),
                row.warehouseId(),
                row.sku(),
                row.physicalQty(),
                row.reservedQty(),
                row.reorderPoint(),
                row.safetyStock(),
                StockItemStatus.valueOf(row.status()),
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public StockItemRow toRow(StockItem stockItem) {
        return new StockItemRow(
                stockItem.stockItemId(),
                stockItem.tenantId(),
                stockItem.warehouseId(),
                stockItem.sku(),
                stockItem.physicalQty(),
                stockItem.reservedQty(),
                stockItem.reorderPoint(),
                stockItem.safetyStock(),
                stockItem.status().name(),
                stockItem.version(),
                stockItem.createdAt(),
                stockItem.updatedAt());
    }

    public StockReservation toDomain(StockReservationRow row) {
        return new StockReservation(
                row.reservationId(),
                row.tenantId(),
                row.stockItemId(),
                row.warehouseId(),
                row.sku(),
                row.cartId(),
                row.orderId(),
                row.qty(),
                StockReservationStatus.valueOf(row.status()),
                row.expiresAt(),
                row.confirmedAt(),
                row.releasedAt(),
                row.createdAt(),
                row.updatedAt());
    }

    public StockReservationRow toRow(StockReservation reservation) {
        return new StockReservationRow(
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

    public StockMovement toDomain(StockMovementRow row) {
        return new StockMovement(
                row.movementId(),
                row.tenantId(),
                row.stockItemId(),
                row.warehouseId(),
                row.sku(),
                StockMovementType.valueOf(row.movementType()),
                row.deltaQty(),
                row.reason(),
                row.reservationId(),
                row.orderId(),
                row.correlationId(),
                row.createdAt());
    }

    public StockMovementRow toRow(StockMovement movement) {
        return new StockMovementRow(
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

    public ReservationLedger toDomain(ReservationLedgerRow row) {
        return new ReservationLedger(
                row.ledgerId(),
                row.tenantId(),
                row.reservationId(),
                row.entryType(),
                row.qty(),
                row.note(),
                row.createdAt());
    }

    public ReservationLedgerRow toRow(ReservationLedger ledger) {
        return new ReservationLedgerRow(
                ledger.ledgerId(),
                ledger.tenantId(),
                ledger.reservationId(),
                ledger.entryType(),
                ledger.qty(),
                ledger.note(),
                ledger.createdAt());
    }

    public IdempotencyRecord toDomain(IdempotencyRecordRow row) {
        return new IdempotencyRecord(
                row.idempotencyId(),
                row.tenantId(),
                row.operationName(),
                row.idempotencyKey(),
                row.requestHash(),
                row.resourceType(),
                row.resourceId(),
                row.responseStatus() == null ? 200 : row.responseStatus(),
                row.createdAt(),
                row.updatedAt());
    }

    public IdempotencyRecordRow toRow(IdempotencyRecord record) {
        return new IdempotencyRecordRow(
                record.idempotencyId(),
                record.tenantId(),
                record.operationName(),
                record.idempotencyKey(),
                record.requestHash(),
                record.resourceType(),
                record.resourceId(),
                record.responseStatus(),
                record.createdAt(),
                record.updatedAt());
    }

    public InventoryAudit toDomain(InventoryAuditRow row) {
        return new InventoryAudit(
                row.auditId(),
                row.tenantId(),
                row.actorUserId(),
                row.actionType(),
                row.targetType(),
                row.targetId(),
                row.outcome(),
                row.payload(),
                row.createdAt());
    }

    public InventoryAuditRow toRow(InventoryAudit audit) {
        return new InventoryAuditRow(
                audit.auditId(),
                audit.tenantId(),
                audit.actorUserId(),
                audit.actionType(),
                audit.targetType(),
                audit.targetId(),
                audit.outcome(),
                audit.payload(),
                audit.createdAt());
    }
}
