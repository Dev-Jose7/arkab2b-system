package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.DirectoryContactPersistencePort;
import com.arka.directory.domain.organizationcontext.entity.OrganizationContact;
import com.arka.directory.domain.organizationcontext.enumtype.ContactType;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveOrganizationContactRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DirectoryContactR2dbcAdapter implements DirectoryContactPersistencePort {

    private final ReactiveOrganizationContactRepository repository;
    private final DirectoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public DirectoryContactR2dbcAdapter(
            ReactiveOrganizationContactRepository repository,
            DirectoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<OrganizationContact> upsert(OrganizationContact contact) {
        var row = rowMapper.toRow(contact);
        return repository.existsById(row.contactId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<OrganizationContact> findById(String organizationId, String contactId) {
        return repository.findByOrganizationIdAndContactId(organizationId, contactId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<OrganizationContact> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Void> clearPrimaryForType(String organizationId, ContactType contactType) {
        return repository.clearPrimaryForType(organizationId, contactType.name()).then();
    }

    @Override
    public Mono<Boolean> existsActiveValue(String organizationId, ContactType contactType, String valueNormalized, String excludingContactId) {
        return repository.existsActiveValue(
                organizationId,
                contactType.name(),
                valueNormalized,
                excludingContactId == null ? "" : excludingContactId);
    }

    @Override
    public Mono<Long> countActive() {
        return repository.countActive();
    }
}
