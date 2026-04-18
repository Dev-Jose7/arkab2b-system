package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.SalesProjectionRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveSalesProjectionRepository extends ReactiveCrudRepository<SalesProjectionRow, String> {

    @Query("SELECT * FROM sales_projections WHERE organization_id = :organizationId AND period = :period")
    Mono<SalesProjectionRow> findByOrganizationAndPeriod(String organizationId, String period);
}
