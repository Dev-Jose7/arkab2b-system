package com.arka.directory.infrastructure.adapter.out.persistence.repository;

import com.arka.directory.infrastructure.adapter.out.persistence.entity.AddressRow;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveAddressRepository extends ReactiveCrudRepository<AddressRow, String> {

    Mono<AddressRow> findByOrganizationIdAndAddressId(String organizationId, String addressId);

    Flux<AddressRow> findByOrganizationId(String organizationId);

    @Modifying
    @Query("""
            UPDATE address
            SET is_default = FALSE,
                updated_at = NOW()
            WHERE organization_id = :organizationId
              AND address_type = :addressType
              AND status = 'ACTIVE'
              AND is_default = TRUE
            """)
    Mono<Integer> clearDefaultForType(@Param("organizationId") String organizationId, @Param("addressType") String addressType);

    @Query("SELECT COUNT(*) FROM address WHERE status = 'ACTIVE'")
    Mono<Long> countActive();
}
