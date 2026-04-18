package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.PaymentRecordEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentRecordR2dbcRepository extends ReactiveCrudRepository<PaymentRecordEntity, String> {

    @Query("""
            SELECT *
            FROM payment_records
            WHERE order_id = :orderId
            ORDER BY created_at ASC
            """)
    Flux<PaymentRecordEntity> findByOrderId(String orderId);

    @Modifying
    @Query("DELETE FROM payment_records WHERE order_id = :orderId")
    Mono<Integer> deleteByOrderId(String orderId);
}
