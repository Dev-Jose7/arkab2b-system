package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.CartEntity;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CartR2dbcRepository extends ReactiveCrudRepository<CartEntity, String> {

    @Query("""
            SELECT *
            FROM carts
            WHERE tenant_id = :tenantId
              AND cart_id = :cartId
            """)
    Mono<CartEntity> findByTenantAndCartId(String tenantId, String cartId);

    @Query("""
            SELECT *
            FROM carts
            WHERE tenant_id = :tenantId
              AND organization_id = :organizationId
              AND user_id = :userId
              AND status IN ('ACTIVE', 'CHECKOUT_IN_PROGRESS')
            ORDER BY created_at DESC
            LIMIT 1
            """)
    Mono<CartEntity> findActiveByTenantOrganizationUser(String tenantId, String organizationId, String userId);

    @Modifying
    @Query("""
            UPDATE carts
               SET status = :status,
                   version = :nextVersion,
                   updated_at = :updatedAt
             WHERE tenant_id = :tenantId
               AND cart_id = :cartId
               AND version = :expectedVersion
            """)
    Mono<Integer> updateWithExpectedVersion(
            String tenantId,
            String cartId,
            String status,
            long nextVersion,
            long expectedVersion,
            Instant updatedAt);
}
