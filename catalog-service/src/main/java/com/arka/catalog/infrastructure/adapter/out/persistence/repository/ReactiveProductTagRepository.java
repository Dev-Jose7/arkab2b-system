package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.ProductTagRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveProductTagRepository extends ReactiveCrudRepository<ProductTagRow, String> {

    @Query("SELECT * FROM product_tags WHERE tenant_id = :tenantId AND product_id = :productId ORDER BY created_at")
    Flux<ProductTagRow> findByTenantAndProduct(String tenantId, String productId);

    @Query("DELETE FROM product_tags WHERE tenant_id = :tenantId AND product_id = :productId")
    Mono<Integer> deleteByTenantAndProduct(String tenantId, String productId);
}
