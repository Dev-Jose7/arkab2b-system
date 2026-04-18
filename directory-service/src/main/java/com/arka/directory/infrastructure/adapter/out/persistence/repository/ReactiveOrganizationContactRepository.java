package com.arka.directory.infrastructure.adapter.out.persistence.repository;

import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationContactRow;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveOrganizationContactRepository extends ReactiveCrudRepository<OrganizationContactRow, String> {

    Mono<OrganizationContactRow> findByOrganizationIdAndContactId(String organizationId, String contactId);

    Flux<OrganizationContactRow> findByOrganizationId(String organizationId);

    @Modifying
    @Query("""
            UPDATE organization_contact
            SET is_primary = FALSE,
                updated_at = NOW()
            WHERE organization_id = :organizationId
              AND contact_type = :contactType
              AND status = 'ACTIVE'
              AND is_primary = TRUE
            """)
    Mono<Integer> clearPrimaryForType(@Param("organizationId") String organizationId, @Param("contactType") String contactType);

    @Query("""
            SELECT EXISTS(
                SELECT 1
                FROM organization_contact
                WHERE organization_id = :organizationId
                  AND contact_type = :contactType
                  AND value_normalized = :valueNormalized
                  AND status = 'ACTIVE'
                  AND contact_id <> :excludingContactId
            )
            """)
    Mono<Boolean> existsActiveValue(
            @Param("organizationId") String organizationId,
            @Param("contactType") String contactType,
            @Param("valueNormalized") String valueNormalized,
            @Param("excludingContactId") String excludingContactId);

    @Query("SELECT COUNT(*) FROM organization_contact WHERE status = 'ACTIVE'")
    Mono<Long> countActive();
}
