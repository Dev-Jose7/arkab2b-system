package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderStatusHistoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderStatusHistoryR2dbcRepository extends ReactiveCrudRepository<OrderStatusHistoryEntity, String> {

    @Query("""
            SELECT *
            FROM order_status_histories
            WHERE tenant_id = :tenantId
              AND order_id = :orderId
            ORDER BY occurred_at ASC
            """)
    Flux<OrderStatusHistoryEntity> findByTenantAndOrderId(String tenantId, String orderId);
}
