package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.OperationsKpiProjectionRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReactiveOperationsKpiProjectionRepository extends ReactiveCrudRepository<OperationsKpiProjectionRow, String> {

    @Query("""
            SELECT *
            FROM operations_kpi_projections
            WHERE tenant_id = :tenantId
              AND period = :period
            ORDER BY kpi_name ASC
            """)
    Flux<OperationsKpiProjectionRow> findByTenantAndPeriod(String tenantId, String period);
}
