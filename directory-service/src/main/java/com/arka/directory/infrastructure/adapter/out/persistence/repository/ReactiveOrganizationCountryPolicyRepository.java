package com.arka.directory.infrastructure.adapter.out.persistence.repository;

import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationCountryPolicyRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveOrganizationCountryPolicyRepository extends ReactiveCrudRepository<OrganizationCountryPolicyRow, String> {

    @Modifying
    @Query("""
            INSERT INTO organization_country_policy (
                policy_id,
                organization_id,
                country_code,
                policy_version,
                currency_code,
                week_starts_on,
                weekly_cutoff_local_time,
                timezone,
                reporting_retention_days,
                requires_verified_address,
                effective_from,
                effective_to,
                status,
                created_at,
                updated_at
            ) VALUES (
                :policyId,
                :organizationId,
                :countryCode,
                :policyVersion,
                :currencyCode,
                :weekStartsOn,
                :weeklyCutoffLocalTime,
                :timezone,
                :reportingRetentionDays,
                :requiresVerifiedAddress,
                :effectiveFrom,
                :effectiveTo,
                :status,
                :createdAt,
                :updatedAt
            )
            """)
    Mono<Integer> insert(
            @Param("policyId") String policyId,
            @Param("organizationId") String organizationId,
            @Param("countryCode") String countryCode,
            @Param("policyVersion") Long policyVersion,
            @Param("currencyCode") String currencyCode,
            @Param("weekStartsOn") String weekStartsOn,
            @Param("weeklyCutoffLocalTime") String weeklyCutoffLocalTime,
            @Param("timezone") String timezone,
            @Param("reportingRetentionDays") Integer reportingRetentionDays,
            @Param("requiresVerifiedAddress") Boolean requiresVerifiedAddress,
            @Param("effectiveFrom") Instant effectiveFrom,
            @Param("effectiveTo") Instant effectiveTo,
            @Param("status") String status,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);

    Mono<OrganizationCountryPolicyRow> findFirstByOrganizationIdAndCountryCodeAndStatusOrderByPolicyVersionDesc(
            String organizationId,
            String countryCode,
            String status);

    Mono<OrganizationCountryPolicyRow> findFirstByOrganizationIdAndCountryCodeOrderByPolicyVersionDesc(
            String organizationId,
            String countryCode);

    Flux<OrganizationCountryPolicyRow> findByOrganizationIdAndStatus(String organizationId, String status);

    @Modifying
    @Query("""
            UPDATE organization_country_policy
            SET status = 'SUPERSEDED',
                effective_to = :effectiveTo,
                updated_at = :updatedAt
            WHERE organization_id = :organizationId
              AND country_code = :countryCode
              AND status = 'ACTIVE'
            """)
    Mono<Integer> supersedeActiveByOrganizationAndCountry(
            @Param("organizationId") String organizationId,
            @Param("countryCode") String countryCode,
            @Param("effectiveTo") Instant effectiveTo,
            @Param("updatedAt") Instant updatedAt);

    @Query("SELECT COUNT(*) FROM organization_country_policy WHERE status = :status")
    Mono<Long> countByStatus(@Param("status") String status);
}
