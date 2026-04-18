package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.VariantRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveVariantRepository extends ReactiveCrudRepository<VariantRow, String> {

    @Query("SELECT * FROM variants WHERE tenant_id = :tenantId AND variant_id = :variantId")
    Mono<VariantRow> findByTenantAndId(String tenantId, String variantId);

    @Query("SELECT * FROM variants WHERE tenant_id = :tenantId AND UPPER(sku) = UPPER(:sku)")
    Mono<VariantRow> findByTenantAndSku(String tenantId, String sku);

    @Query("SELECT * FROM variants WHERE tenant_id = :tenantId AND product_id = :productId ORDER BY created_at DESC")
    Flux<VariantRow> findByTenantAndProduct(String tenantId, String productId);

    @Query("""
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM variants
            WHERE tenant_id = :tenantId
              AND UPPER(sku) = UPPER(:sku)
              AND status = 'SELLABLE'
              AND (:excludingVariantId IS NULL OR variant_id <> :excludingVariantId)
            """)
    Mono<Boolean> existsSellableSku(String tenantId, String sku, String excludingVariantId);
}
