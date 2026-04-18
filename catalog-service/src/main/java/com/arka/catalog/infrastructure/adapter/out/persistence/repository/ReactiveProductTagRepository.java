package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.ProductTagRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveProductTagRepository extends ReactiveCrudRepository<ProductTagRow, String> {

    @Query("SELECT * FROM product_tags WHERE organization_id = :organizationId AND product_id = :productId ORDER BY created_at")
    Flux<ProductTagRow> findByOrganizationAndProduct(String organizationId, String productId);

    @Query("DELETE FROM product_tags WHERE organization_id = :organizationId AND product_id = :productId")
    Mono<Integer> deleteByOrganizationAndProduct(String organizationId, String productId);
}
