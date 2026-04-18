package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.AnalyticFactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveAnalyticFactRepository extends ReactiveCrudRepository<AnalyticFactRow, String> {

    @Query("SELECT * FROM analytic_facts WHERE organization_id = :organizationId AND fact_id = :factId")
    Mono<AnalyticFactRow> findByOrganizationAndId(String organizationId, String factId);

    @Query("SELECT * FROM analytic_facts WHERE organization_id = :organizationId AND source_event_id = :sourceEventId")
    Mono<AnalyticFactRow> findByOrganizationAndSourceEventId(String organizationId, String sourceEventId);

    @Query("SELECT * FROM analytic_facts WHERE organization_id = :organizationId AND fact_status = 'APPLIED' ORDER BY occurred_at ASC")
    Flux<AnalyticFactRow> findAppliedByOrganization(String organizationId);
}
