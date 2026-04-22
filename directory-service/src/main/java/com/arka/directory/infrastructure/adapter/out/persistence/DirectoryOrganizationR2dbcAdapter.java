package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.DirectoryOrganizationPersistencePort;
import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveOrganizationRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DirectoryOrganizationR2dbcAdapter implements DirectoryOrganizationPersistencePort {

    private final ReactiveOrganizationRepository repository;
    private final DirectoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public DirectoryOrganizationR2dbcAdapter(
            ReactiveOrganizationRepository repository,
            DirectoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Organization> save(Organization organization) {
        var row = rowMapper.toRow(organization);
        return repository.existsById(row.organizationId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Organization> findById(String organizationId) {
        return repository.findById(organizationId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return repository.count();
    }

    @Override
    public Mono<Long> countByStatus(String status) {
        return repository.countByStatus(status);
    }
}
