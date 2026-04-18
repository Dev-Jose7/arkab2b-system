package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.VariantAttributeRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveVariantAttributeRepository extends ReactiveCrudRepository<VariantAttributeRow, String> {

    @Query("SELECT * FROM variant_attributes WHERE tenant_id = :tenantId AND variant_id = :variantId ORDER BY attribute_code")
    Flux<VariantAttributeRow> findByTenantAndVariant(String tenantId, String variantId);

    @Query("DELETE FROM variant_attributes WHERE tenant_id = :tenantId AND variant_id = :variantId")
    Mono<Integer> deleteByTenantAndVariant(String tenantId, String variantId);
}
