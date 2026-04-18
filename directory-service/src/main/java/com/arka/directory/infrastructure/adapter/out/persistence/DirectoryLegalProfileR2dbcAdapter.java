package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.DirectoryLegalProfilePersistencePort;
import com.arka.directory.domain.organizationcontext.entity.OrganizationLegalProfile;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveOrganizationLegalProfileRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DirectoryLegalProfileR2dbcAdapter implements DirectoryLegalProfilePersistencePort {

    private final ReactiveOrganizationLegalProfileRepository repository;
    private final DirectoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public DirectoryLegalProfileR2dbcAdapter(
            ReactiveOrganizationLegalProfileRepository repository,
            DirectoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<OrganizationLegalProfile> upsert(OrganizationLegalProfile legalProfile) {
        var row = rowMapper.toRow(legalProfile);
        return repository.existsById(row.legalProfileId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<OrganizationLegalProfile> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsActiveOrganizationWithTaxId(String countryCode, String taxId, String excludingOrganizationId) {
        return repository.existsActiveOrganizationWithTaxId(countryCode, taxId, excludingOrganizationId == null ? "" : excludingOrganizationId);
    }
}
