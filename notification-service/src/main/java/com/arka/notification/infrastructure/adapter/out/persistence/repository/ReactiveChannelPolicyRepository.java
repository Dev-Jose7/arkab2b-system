package com.arka.notification.infrastructure.adapter.out.persistence.repository;

import com.arka.notification.infrastructure.adapter.out.persistence.entity.ChannelPolicyRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveChannelPolicyRepository extends ReactiveCrudRepository<ChannelPolicyRow, String> {

    @Query("""
            SELECT *
            FROM channel_policies
            WHERE organization_id = :organizationId
              AND source_event_type = :sourceEventType
              AND active = TRUE
            LIMIT 1
            """)
    Mono<ChannelPolicyRow> findActive(String organizationId, String sourceEventType);
}
