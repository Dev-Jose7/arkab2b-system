package com.arka.inventory.infrastructure.adapter.in.web.mapper.command;

import com.arka.inventory.application.command.ConfirmReservationCommand;
import com.arka.inventory.application.command.CreateWarehouseCommand;
import com.arka.inventory.application.command.ExpireReservationsCommand;
import com.arka.inventory.application.command.InitializeStockItemCommand;
import com.arka.inventory.application.command.RecalculateCommitableAvailabilityCommand;
import com.arka.inventory.application.command.ReleaseReservationCommand;
import com.arka.inventory.application.command.ReserveStockCommand;
import com.arka.inventory.application.command.UpdateOperationalStockCommand;
import com.arka.inventory.application.command.UpdateStockItemStatusCommand;
import com.arka.inventory.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.inventory.infrastructure.adapter.in.web.request.ConfirmReservationRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.CreateWarehouseRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.ExpireReservationsRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.InitializeStockItemRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.RecalculateCommitableAvailabilityRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.ReleaseReservationRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.ReserveStockRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.UpdateOperationalStockRequest;
import com.arka.inventory.infrastructure.adapter.in.web.request.UpdateStockItemStatusRequest;
import org.springframework.stereotype.Component;

@Component
public class InventoryCommandMapper {

    public CreateWarehouseCommand toCommand(
            CreateWarehouseRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new CreateWarehouseCommand(
                principal.organizationId(),
                request.warehouseCode(),
                request.warehouseName(),
                request.countryCode(),
                principal.userId(),
                idempotencyKey);
    }

    public InitializeStockItemCommand toCommand(
            InitializeStockItemRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new InitializeStockItemCommand(
                principal.organizationId(),
                request.warehouseId(),
                request.sku(),
                request.initialPhysicalQty(),
                request.reorderPoint(),
                request.safetyStock(),
                principal.userId(),
                idempotencyKey);
    }

    public UpdateOperationalStockCommand toCommand(
            String stockItemId,
            UpdateOperationalStockRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new UpdateOperationalStockCommand(
                principal.organizationId(),
                stockItemId,
                request.deltaQty(),
                request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public ReserveStockCommand toCommand(
            String stockItemId,
            ReserveStockRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new ReserveStockCommand(
                principal.organizationId(),
                stockItemId,
                request.cartId(),
                request.qty(),
                request.expiresAt(),
                principal.userId(),
                idempotencyKey);
    }

    public ConfirmReservationCommand toCommand(
            String reservationId,
            ConfirmReservationRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new ConfirmReservationCommand(
                principal.organizationId(),
                reservationId,
                request.orderId(),
                principal.userId(),
                idempotencyKey);
    }

    public ReleaseReservationCommand toCommand(
            String reservationId,
            ReleaseReservationRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new ReleaseReservationCommand(
                principal.organizationId(),
                reservationId,
                request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public ExpireReservationsCommand toCommand(
            ExpireReservationsRequest request,
            IamSecurityPrincipal principal) {
        Integer batchSize = request == null ? null : request.batchSize();
        return new ExpireReservationsCommand(
                principal.organizationId(),
                batchSize,
                principal.userId());
    }

    public RecalculateCommitableAvailabilityCommand toCommand(
            String stockItemId,
            RecalculateCommitableAvailabilityRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new RecalculateCommitableAvailabilityCommand(
                principal.organizationId(),
                stockItemId,
                request == null ? null : request.reason(),
                principal.userId(),
                idempotencyKey);
    }

    public UpdateStockItemStatusCommand toCommand(
            String stockItemId,
            UpdateStockItemStatusRequest request,
            IamSecurityPrincipal principal,
            String idempotencyKey) {
        return new UpdateStockItemStatusCommand(
                principal.organizationId(),
                stockItemId,
                request.targetStatus(),
                request.reason(),
                principal.userId(),
                idempotencyKey);
    }
}
