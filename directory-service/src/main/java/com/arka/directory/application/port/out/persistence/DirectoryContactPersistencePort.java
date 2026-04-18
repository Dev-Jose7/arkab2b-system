package com.arka.directory.application.port.out.persistence;

import com.arka.directory.domain.organizationcontext.entity.OrganizationContact;
import com.arka.directory.domain.organizationcontext.enumtype.ContactType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DirectoryContactPersistencePort {

    Mono<OrganizationContact> upsert(OrganizationContact contact);

    Mono<OrganizationContact> findById(String organizationId, String contactId);

    Flux<OrganizationContact> findByOrganizationId(String organizationId);

    Mono<Void> clearPrimaryForType(String organizationId, ContactType contactType);

    Mono<Boolean> existsActiveValue(String organizationId, ContactType contactType, String valueNormalized, String excludingContactId);

    Mono<Long> countActive();
}
