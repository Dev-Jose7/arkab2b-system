package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.PriceRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactivePriceRepository extends ReactiveCrudRepository<PriceRow, String> {

    @Query("SELECT * FROM prices WHERE organization_id = :organizationId AND price_id = :priceId")
    Mono<PriceRow> findByOrganizationAndId(String organizationId, String priceId);

    @Query("""
            SELECT *
            FROM prices
            WHERE organization_id = :organizationId
              AND variant_id = :variantId
              AND currency = :currency
              AND price_type = :priceType
            ORDER BY effective_from DESC
            """)
    Flux<PriceRow> findTimeline(String organizationId, String variantId, String currency, String priceType);

    @Query("""
            SELECT *
            FROM prices
            WHERE organization_id = :organizationId
              AND variant_id = :variantId
              AND currency = :currency
              AND price_type = :priceType
              AND effective_from <= :at
              AND (effective_until IS NULL OR effective_until > :at)
            ORDER BY effective_from DESC
            LIMIT 1
            """)
    Mono<PriceRow> resolveActive(String organizationId, String variantId, String currency, String priceType, Instant at);

    @Query("""
            SELECT *
            FROM prices
            WHERE organization_id = :organizationId
              AND variant_id = :variantId
            ORDER BY effective_from DESC
            LIMIT 1
            """)
    Mono<PriceRow> findLatestByVariant(String organizationId, String variantId);
}
