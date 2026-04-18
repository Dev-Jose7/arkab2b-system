package com.arka.inventory.infrastructure.adapter.in.web.mapper.query;

import com.arka.inventory.application.query.GetCommitableAvailabilityQuery;
import com.arka.inventory.application.query.GetInventoryAuditQuery;
import com.arka.inventory.application.query.GetLowStockQuery;
import com.arka.inventory.application.query.GetStockItemQuery;
import com.arka.inventory.application.query.GetStockMovementsQuery;
import com.arka.inventory.application.query.ListReservationsByCartQuery;
import com.arka.inventory.application.query.ListStockByWarehouseQuery;
import com.arka.inventory.application.query.ResolveCheckoutAvailabilityQuery;
import com.arka.inventory.application.query.ValidateReservationReferenceQuery;
import com.arka.inventory.infrastructure.adapter.in.security.IamSecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
public class InventoryQueryMapper {

    public GetStockItemQuery toQuery(String stockItemId, IamSecurityPrincipal principal) {
        return new GetStockItemQuery(
                principal.organizationId(),
                stockItemId,
                principal.userId());
    }

    public GetCommitableAvailabilityQuery toQuery(String warehouseId, String sku, IamSecurityPrincipal principal) {
        return new GetCommitableAvailabilityQuery(
                principal.organizationId(),
                warehouseId,
                sku,
                principal.userId());
    }

    public ListStockByWarehouseQuery toListStockQuery(String warehouseId, IamSecurityPrincipal principal) {
        return new ListStockByWarehouseQuery(
                principal.organizationId(),
                warehouseId,
                principal.userId());
    }

    public ListReservationsByCartQuery toListReservationsByCartQuery(String cartId, IamSecurityPrincipal principal) {
        return new ListReservationsByCartQuery(
                principal.organizationId(),
                cartId,
                principal.userId());
    }

    public GetStockMovementsQuery toStockMovementsQuery(String stockItemId, Integer limit, IamSecurityPrincipal principal) {
        return new GetStockMovementsQuery(
                principal.organizationId(),
                stockItemId,
                limit,
                principal.userId());
    }

    public GetLowStockQuery toLowStockQuery(String warehouseId, IamSecurityPrincipal principal) {
        return new GetLowStockQuery(
                principal.organizationId(),
                warehouseId,
                principal.userId());
    }

    public GetInventoryAuditQuery toAuditQuery(Integer limit, IamSecurityPrincipal principal) {
        return new GetInventoryAuditQuery(
                principal.organizationId(),
                limit,
                principal.userId());
    }

    public ResolveCheckoutAvailabilityQuery toCheckoutAvailabilityQuery(
            String stockItemId,
            Integer requestedQty,
            IamSecurityPrincipal principal) {
        return new ResolveCheckoutAvailabilityQuery(
                principal.organizationId(),
                stockItemId,
                requestedQty,
                principal.userId());
    }

    public ValidateReservationReferenceQuery toReservationValidationQuery(
            String organizationId,
            String reservationId,
            String sku,
            Integer qty) {
        return new ValidateReservationReferenceQuery(
                organizationId,
                reservationId,
                sku,
                qty == null ? 0 : qty);
    }
}
