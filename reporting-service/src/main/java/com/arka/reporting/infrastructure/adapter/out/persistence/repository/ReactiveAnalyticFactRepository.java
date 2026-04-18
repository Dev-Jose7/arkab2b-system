package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.AnalyticFactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveAnalyticFactRepository extends ReactiveCrudRepository<AnalyticFactRow, String> {

    @Query("SELECT * FROM analytic_facts WHERE tenant_id = :tenantId AND fact_id = :factId")
    Mono<AnalyticFactRow> findByTenantAndId(String tenantId, String factId);

    @Query("SELECT * FROM analytic_facts WHERE tenant_id = :tenantId AND source_event_id = :sourceEventId")
    Mono<AnalyticFactRow> findByTenantAndSourceEventId(String tenantId, String sourceEventId);

    @Query("SELECT * FROM analytic_facts WHERE tenant_id = :tenantId AND fact_status = 'APPLIED' ORDER BY occurred_at ASC")
    Flux<AnalyticFactRow> findAppliedByTenant(String tenantId);
}
