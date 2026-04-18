package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.DirectoryCountryPolicyPersistencePort;
import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveOrganizationCountryPolicyRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DirectoryCountryPolicyR2dbcAdapter implements DirectoryCountryPolicyPersistencePort {

    private final ReactiveOrganizationCountryPolicyRepository repository;
    private final DirectoryRowMapper rowMapper;

    public DirectoryCountryPolicyR2dbcAdapter(
            ReactiveOrganizationCountryPolicyRepository repository,
            DirectoryRowMapper rowMapper) {
        this.repository = repository;
        this.rowMapper = rowMapper;
    }

    @Override
    public Mono<CountryPolicy> save(CountryPolicy countryPolicy) {
        var row = rowMapper.toRow(countryPolicy);
        return repository
                .insert(
                        row.policyId(),
                        row.organizationId(),
                        row.countryCode(),
                        row.policyVersion(),
                        row.currencyCode(),
                        row.weekStartsOn(),
                        row.weeklyCutoffLocalTime(),
                        row.timezone(),
                        row.reportingRetentionDays(),
                        row.requiresVerifiedAddress(),
                        row.effectiveFrom(),
                        row.effectiveTo(),
                        row.status(),
                        row.createdAt(),
                        row.updatedAt())
                .then(repository.findById(row.policyId()))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<CountryPolicy> findActiveByOrganizationAndCountry(String organizationId, String countryCode) {
        return repository
                .findFirstByOrganizationIdAndCountryCodeAndStatusOrderByPolicyVersionDesc(
                        organizationId,
                        countryCode,
                        CountryPolicyStatus.ACTIVE.name())
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<CountryPolicy> findLatestByOrganizationAndCountry(String organizationId, String countryCode) {
        return repository
                .findFirstByOrganizationIdAndCountryCodeOrderByPolicyVersionDesc(organizationId, countryCode)
                .map(rowMapper::toDomain);
    }

    @Override
    public Flux<CountryPolicy> findActiveByOrganization(String organizationId) {
        return repository.findByOrganizationIdAndStatus(organizationId, CountryPolicyStatus.ACTIVE.name()).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Void> supersedeActiveByOrganizationAndCountry(String organizationId, String countryCode) {
        java.time.Instant now = java.time.Instant.now();
        return repository.supersedeActiveByOrganizationAndCountry(organizationId, countryCode, now, now).then();
    }

    @Override
    public Mono<Long> countActive() {
        return repository.countByStatus(CountryPolicyStatus.ACTIVE.name());
    }
}
