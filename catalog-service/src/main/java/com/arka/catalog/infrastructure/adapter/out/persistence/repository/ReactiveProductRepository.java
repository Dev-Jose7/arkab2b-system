package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.ProductRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveProductRepository extends ReactiveCrudRepository<ProductRow, String> {

    @Query("SELECT * FROM products WHERE tenant_id = :tenantId AND product_id = :productId")
    Mono<ProductRow> findByTenantAndId(String tenantId, String productId);

    @Query("""
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM products
            WHERE tenant_id = :tenantId
              AND UPPER(product_code) = UPPER(:productCode)
              AND (:excludingProductId IS NULL OR product_id <> :excludingProductId)
            """)
    Mono<Boolean> existsByProductCode(String tenantId, String productCode, String excludingProductId);
}
