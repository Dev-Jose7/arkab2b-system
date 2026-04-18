package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.DirectoryCountryPolicyPersistencePort;
import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.countrypolicy.repository.CountryPolicyRepository;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DomainCountryPolicyRepositoryAdapter implements CountryPolicyRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final DirectoryCountryPolicyPersistencePort persistencePort;

    public DomainCountryPolicyRepositoryAdapter(DirectoryCountryPolicyPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public CountryPolicy save(CountryPolicy countryPolicy) {
        CountryPolicy saved = persistencePort.save(countryPolicy).block(BLOCK_TIMEOUT);
        if (saved == null) {
            throw new IllegalStateException("CountryPolicy persistence returned empty result");
        }
        return saved;
    }

    @Override
    public Optional<CountryPolicy> findActiveByOrganizationAndCountry(OrganizationId organizationId, CountryCode countryCode) {
        return persistencePort
                .findActiveByOrganizationAndCountry(organizationId.value(), countryCode.value())
                .blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public Optional<CountryPolicy> findLatestByOrganizationAndCountry(OrganizationId organizationId, CountryCode countryCode) {
        return persistencePort
                .findLatestByOrganizationAndCountry(organizationId.value(), countryCode.value())
                .blockOptional(BLOCK_TIMEOUT);
    }
}
