package com.arka.directory.application.port.out.persistence;

import com.arka.directory.domain.organizationcontext.entity.OrganizationLegalProfile;
import reactor.core.publisher.Mono;

public interface DirectoryLegalProfilePersistencePort {

    Mono<OrganizationLegalProfile> upsert(OrganizationLegalProfile legalProfile);

    Mono<OrganizationLegalProfile> findByOrganizationId(String organizationId);

    Mono<Boolean> existsActiveOrganizationWithTaxId(String countryCode, String taxId, String excludingOrganizationId);
}
