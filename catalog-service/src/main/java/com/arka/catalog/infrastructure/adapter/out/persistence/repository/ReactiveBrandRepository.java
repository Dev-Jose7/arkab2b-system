package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.BrandRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveBrandRepository extends ReactiveCrudRepository<BrandRow, String> {

    @Query("""
            SELECT *
            FROM brands
            WHERE organization_id = :organizationId
              AND status = 'ACTIVE'
            ORDER BY brand_name ASC
            """)
    Flux<BrandRow> findActiveByOrganization(String organizationId);

    @Query("""
            SELECT *
            FROM brands
            WHERE organization_id = :organizationId
              AND brand_id = :brandId
              AND status = 'ACTIVE'
            """)
    Mono<BrandRow> findActive(String organizationId, String brandId);
}
