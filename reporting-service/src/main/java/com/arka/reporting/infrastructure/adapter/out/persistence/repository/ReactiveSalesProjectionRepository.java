package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.SalesProjectionRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveSalesProjectionRepository extends ReactiveCrudRepository<SalesProjectionRow, String> {

    @Query("SELECT * FROM sales_projections WHERE tenant_id = :tenantId AND period = :period")
    Mono<SalesProjectionRow> findByTenantAndPeriod(String tenantId, String period);
}
