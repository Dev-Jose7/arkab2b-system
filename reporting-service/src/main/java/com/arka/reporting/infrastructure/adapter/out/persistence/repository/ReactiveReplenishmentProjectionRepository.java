package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ReplenishmentProjectionRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReactiveReplenishmentProjectionRepository extends ReactiveCrudRepository<ReplenishmentProjectionRow, String> {

    @Query("""
            SELECT *
            FROM replenishment_projections
            WHERE organization_id = :organizationId
              AND period = :period
              AND (:sku IS NULL OR sku = :sku)
            ORDER BY risk_level DESC, sku ASC
            OFFSET :offset
            LIMIT :limit
            """)
    Flux<ReplenishmentProjectionRow> findByOrganizationAndPeriod(String organizationId, String period, String sku, int offset, int limit);
}
