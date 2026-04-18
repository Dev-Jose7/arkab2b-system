package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderAuditEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderAuditR2dbcRepository extends ReactiveCrudRepository<OrderAuditEntity, String> {

    @Query("""
            SELECT *
            FROM order_audits
            WHERE organization_id = :organizationId
              AND organization_id = :organizationId
              AND target_id = :orderId
            ORDER BY created_at DESC
            LIMIT :limit
            """)
    Flux<OrderAuditEntity> findByOrder(String organizationId, String orderId, int limit);
}
