package com.arka.notification.infrastructure.adapter.out.persistence.repository;

import com.arka.notification.infrastructure.adapter.out.persistence.entity.ProviderCallbackRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveProviderCallbackRepository extends ReactiveCrudRepository<ProviderCallbackRow, String> {

    @Query("""
            SELECT *
            FROM provider_callbacks
            WHERE provider_code = :providerCode
              AND provider_ref = :providerRef
              AND callback_event_id = :callbackEventId
            LIMIT 1
            """)
    Mono<ProviderCallbackRow> findByProviderRefAndEvent(String providerCode, String providerRef, String callbackEventId);

    @Query("""
            SELECT *
            FROM provider_callbacks
            WHERE organization_id = :organizationId
              AND notification_id = :notificationId
            ORDER BY received_at ASC
            """)
    Flux<ProviderCallbackRow> findByNotificationId(String organizationId, String notificationId);
}
