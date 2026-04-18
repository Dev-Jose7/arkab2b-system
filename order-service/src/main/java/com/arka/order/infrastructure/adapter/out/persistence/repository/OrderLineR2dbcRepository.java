package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderLineEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderLineR2dbcRepository extends ReactiveCrudRepository<OrderLineEntity, String> {

    @Query("""
            SELECT *
            FROM order_lines
            WHERE order_id = :orderId
            ORDER BY created_at ASC
            """)
    Flux<OrderLineEntity> findByOrderId(String orderId);

    @Modifying
    @Query("DELETE FROM order_lines WHERE order_id = :orderId")
    Mono<Integer> deleteByOrderId(String orderId);
}
