package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.domain.access.aggregate.PermissionAggregate;
import com.arka.identityaccess.domain.access.enumtype.PermissionScope;
import com.arka.identityaccess.domain.access.enumtype.PermissionStatus;
import com.arka.identityaccess.domain.access.repository.PermissionRepository;
import com.arka.identityaccess.domain.access.valueobject.PermissionAction;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.PermissionId;
import com.arka.identityaccess.domain.access.valueobject.PermissionResource;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.PermissionCatalogRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactivePermissionCatalogRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DomainPermissionRepositoryAdapter implements PermissionRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);
    private static final String DISABLED_PREFIX = "[DISABLED] ";

    private final ReactivePermissionCatalogRepository repository;

    public DomainPermissionRepositoryAdapter(ReactivePermissionCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public PermissionAggregate save(PermissionAggregate permission) {
        Instant now = Instant.now();
        PermissionCatalogRow saved = repository
                .save(new PermissionCatalogRow(
                        permission.id().value(),
                        permission.code().value(),
                        permission.resource().value(),
                        permission.action().value(),
                        permission.scope().name(),
                        encodeDisplayName(permission.displayName(), permission.status()),
                        permission.status().name(),
                        now,
                        now))
                .block(BLOCK_TIMEOUT);

        if (saved == null) {
            throw new IllegalStateException("Permission persistence returned empty result");
        }
        return toDomain(saved);
    }

    @Override
    public Optional<PermissionAggregate> findById(PermissionId permissionId) {
        return repository.findById(permissionId.value()).map(this::toDomain).blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public Optional<PermissionAggregate> findByCode(PermissionCode permissionCode) {
        return repository.findByPermissionCode(permissionCode.value()).map(this::toDomain).blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public boolean existsByCode(PermissionCode permissionCode) {
        return repository.existsByPermissionCode(permissionCode.value()).blockOptional(BLOCK_TIMEOUT).orElse(Boolean.FALSE);
    }

    private PermissionAggregate toDomain(PermissionCatalogRow row) {
        String displayName = decodeDisplayName(row.displayName());
        PermissionStatus status = row.status() == null || row.status().isBlank()
                ? (row.displayName() != null && row.displayName().startsWith(DISABLED_PREFIX)
                        ? PermissionStatus.DISABLED
                        : PermissionStatus.ACTIVE)
                : PermissionStatus.valueOf(row.status().trim().toUpperCase());
        return PermissionAggregate.rehydrate(
                PermissionId.of(row.permissionId()),
                PermissionCode.of(row.permissionCode()),
                PermissionResource.of(row.resource()),
                PermissionAction.of(row.action()),
                PermissionScope.of(row.scope()),
                displayName,
                status);
    }

    private String encodeDisplayName(String displayName, PermissionStatus status) {
        String normalized = (displayName == null || displayName.isBlank()) ? "PERMISSION" : displayName.trim();
        return status == PermissionStatus.DISABLED ? DISABLED_PREFIX + normalized : normalized;
    }

    private String decodeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "PERMISSION";
        }
        return displayName.startsWith(DISABLED_PREFIX)
                ? displayName.substring(DISABLED_PREFIX.length())
                : displayName;
    }
}
