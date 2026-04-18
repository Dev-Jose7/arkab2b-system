package com.arka.directory.domain.organizationcontext.repository;

import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import java.util.Optional;

public interface OrganizationRepository {

    Organization save(Organization organization);

    Optional<Organization> findById(OrganizationId organizationId);

    Optional<Organization> findByCode(String organizationCode);
}
