package com.arka.directory.infrastructure.adapter.out.persistence.repository;

import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveOrganizationRepository extends ReactiveCrudRepository<OrganizationRow, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM organization WHERE UPPER(organization_code) = UPPER(:organizationCode))")
    Mono<Boolean> existsByOrganizationCodeIgnoreCase(String organizationCode);

    @Query("""
            SELECT *
            FROM organization
            WHERE UPPER(organization_code) = UPPER(:organizationCode)
            LIMIT 1
            """)
    Mono<OrganizationRow> findByOrganizationCodeIgnoreCase(String organizationCode);

    @Query("SELECT COUNT(*) FROM organization WHERE status = :status")
    Mono<Long> countByStatus(String status);
}
