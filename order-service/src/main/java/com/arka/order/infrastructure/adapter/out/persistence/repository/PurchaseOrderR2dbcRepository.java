package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.PurchaseOrderEntity;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PurchaseOrderR2dbcRepository extends ReactiveCrudRepository<PurchaseOrderEntity, String> {

    @Query("""
            SELECT *
            FROM purchase_orders
            WHERE tenant_id = :tenantId
              AND order_id = :orderId
            """)
    Mono<PurchaseOrderEntity> findByTenantAndOrderId(String tenantId, String orderId);

    @Query("""
            SELECT *
            FROM purchase_orders
            WHERE tenant_id = :tenantId
              AND checkout_correlation_id = :checkoutCorrelationId
            """)
    Mono<PurchaseOrderEntity> findByCheckoutCorrelation(String tenantId, String checkoutCorrelationId);

    @Query("""
            SELECT COUNT(1) > 0
            FROM purchase_orders
            WHERE tenant_id = :tenantId
              AND checkout_correlation_id = :checkoutCorrelationId
            """)
    Mono<Boolean> existsByCheckoutCorrelation(String tenantId, String checkoutCorrelationId);

    @Modifying
    @Query("""
            UPDATE purchase_orders
               SET status = :status,
                   payment_status = :paymentStatus,
                   subtotal = :subtotal,
                   total_amount = :totalAmount,
                   version = :nextVersion,
                   updated_at = :updatedAt
             WHERE tenant_id = :tenantId
               AND order_id = :orderId
               AND version = :expectedVersion
            """)
    Mono<Integer> updateWithExpectedVersion(
            String tenantId,
            String orderId,
            String status,
            String paymentStatus,
            java.math.BigDecimal subtotal,
            java.math.BigDecimal totalAmount,
            long nextVersion,
            long expectedVersion,
            Instant updatedAt);
}
