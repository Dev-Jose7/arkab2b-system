package com.arka.directory.domain.countrypolicy.repository;

import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import java.util.Optional;

public interface CountryPolicyRepository {

    CountryPolicy save(CountryPolicy countryPolicy);

    Optional<CountryPolicy> findActiveByOrganizationAndCountry(OrganizationId organizationId, CountryCode countryCode);

    Optional<CountryPolicy> findLatestByOrganizationAndCountry(OrganizationId organizationId, CountryCode countryCode);
}
