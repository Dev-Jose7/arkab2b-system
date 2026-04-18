package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.CategoryRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveCategoryRepository extends ReactiveCrudRepository<CategoryRow, String> {

    @Query("""
            SELECT *
            FROM categories
            WHERE organization_id = :organizationId
              AND category_id = :categoryId
              AND status = 'ACTIVE'
            """)
    Mono<CategoryRow> findActive(String organizationId, String categoryId);
}
