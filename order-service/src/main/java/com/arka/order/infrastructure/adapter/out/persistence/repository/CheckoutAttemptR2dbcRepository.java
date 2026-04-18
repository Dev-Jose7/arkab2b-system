package com.arka.order.infrastructure.adapter.out.persistence.repository;

import com.arka.order.infrastructure.adapter.out.persistence.entity.CheckoutAttemptEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CheckoutAttemptR2dbcRepository extends ReactiveCrudRepository<CheckoutAttemptEntity, String> {

    @Query("""
            SELECT *
            FROM checkout_attempts
            WHERE organization_id = :organizationId
              AND checkout_correlation_id = :checkoutCorrelationId
            """)
    Mono<CheckoutAttemptEntity> findByCorrelation(String organizationId, String checkoutCorrelationId);
}
