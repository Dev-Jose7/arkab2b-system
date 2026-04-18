package com.arka.directory.infrastructure.adapter.out.persistence.repository;

import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationLegalProfileRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveOrganizationLegalProfileRepository extends ReactiveCrudRepository<OrganizationLegalProfileRow, String> {

    Mono<OrganizationLegalProfileRow> findByOrganizationId(String organizationId);

    @Query("""
            SELECT EXISTS(
                SELECT 1
                FROM organization_legal_profile lp
                JOIN organization o ON o.organization_id = lp.organization_id
                WHERE UPPER(lp.country_code) = UPPER(:countryCode)
                  AND UPPER(lp.tax_id) = UPPER(:taxId)
                  AND o.status = 'ACTIVE'
                  AND lp.organization_id <> :excludingOrganizationId
            )
            """)
    Mono<Boolean> existsActiveOrganizationWithTaxId(String countryCode, String taxId, String excludingOrganizationId);
}
