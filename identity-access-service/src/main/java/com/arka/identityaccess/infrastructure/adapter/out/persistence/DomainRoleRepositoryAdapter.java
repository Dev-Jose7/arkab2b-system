package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.domain.access.aggregate.RoleAggregate;
import com.arka.identityaccess.domain.access.enumtype.RoleStatus;
import com.arka.identityaccess.domain.access.repository.RoleRepository;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.RoleRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveRoleRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class DomainRoleRepositoryAdapter implements RoleRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);
    private static final String DISABLED_PREFIX = "[DISABLED] ";

    private final ReactiveRoleRepository roleRepository;
    private final DatabaseClient databaseClient;

    public DomainRoleRepositoryAdapter(
            ReactiveRoleRepository roleRepository,
            DatabaseClient databaseClient) {
        this.roleRepository = roleRepository;
        this.databaseClient = databaseClient;
    }

    @Override
    public RoleAggregate save(RoleAggregate role) {
        Instant now = Instant.now();
        RoleRow savedRow = roleRepository
                .save(new RoleRow(
                        role.id().value(),
                        role.code().value(),
                        encodeDescription(role.displayName(), role.status()),
                        now,
                        now))
                .block(BLOCK_TIMEOUT);

        if (savedRow == null) {
            throw new IllegalStateException("Role persistence returned empty result");
        }

        syncRolePermissions(savedRow.roleId(), role.permissions(), now);
        return toDomain(savedRow, loadPermissionCodes(savedRow.roleId()));
    }

    @Override
    public Optional<RoleAggregate> findById(RoleId roleId) {
        return roleRepository
                .findById(roleId.value())
                .map(row -> toDomain(row, loadPermissionCodes(row.roleId())))
                .blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public Optional<RoleAggregate> findByCode(RoleCode roleCode) {
        return roleRepository
                .findByRoleCode(roleCode.value())
                .map(row -> toDomain(row, loadPermissionCodes(row.roleId())))
                .blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public boolean existsByCode(RoleCode roleCode) {
        return roleRepository.existsByRoleCode(roleCode.value()).blockOptional(BLOCK_TIMEOUT).orElse(Boolean.FALSE);
    }

    private void syncRolePermissions(String roleId, Set<PermissionCode> permissions, Instant now) {
        databaseClient.sql("DELETE FROM role_permission WHERE role_id = :roleId")
                .bind("roleId", roleId)
                .fetch()
                .rowsUpdated()
                .block(BLOCK_TIMEOUT);

        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        for (PermissionCode permissionCode : permissions) {
            PermissionDefinition definition = parsePermission(permissionCode.value());
            databaseClient.sql("""
                            INSERT INTO role_permission (role_id, permission_code, resource, action, scope, created_at)
                            VALUES (:roleId, :permissionCode, :resource, :action, :scope, :createdAt)
                            ON CONFLICT DO NOTHING
                            """)
                    .bind("roleId", roleId)
                    .bind("permissionCode", permissionCode.value())
                    .bind("resource", definition.resource())
                    .bind("action", definition.action())
                    .bind("scope", definition.scope())
                    .bind("createdAt", now)
                    .fetch()
                    .rowsUpdated()
                    .block(BLOCK_TIMEOUT);
        }
    }

    private Set<PermissionCode> loadPermissionCodes(String roleId) {
        return databaseClient.sql("""
                        SELECT permission_code
                        FROM role_permission
                        WHERE role_id = :roleId
                        ORDER BY permission_code
                        """)
                .bind("roleId", roleId)
                .map((row, metadata) -> row.get("permission_code", String.class))
                .all()
                .filter(code -> code != null && !code.isBlank())
                .map(PermissionCode::of)
                .collectList()
                .map(list -> new LinkedHashSet<>(list))
                .blockOptional(BLOCK_TIMEOUT)
                .map(Set::copyOf)
                .orElseGet(Set::of);
    }

    private RoleAggregate toDomain(RoleRow row, Set<PermissionCode> permissions) {
        boolean disabled = row.description() != null && row.description().startsWith(DISABLED_PREFIX);
        String displayName = row.description() == null
                ? row.roleCode()
                : (disabled ? row.description().substring(DISABLED_PREFIX.length()) : row.description());
        return RoleAggregate.rehydrate(
                RoleId.of(row.roleId()),
                RoleCode.of(row.roleCode()),
                displayName,
                disabled ? RoleStatus.DISABLED : RoleStatus.ACTIVE,
                permissions);
    }

    private String encodeDescription(String displayName, RoleStatus status) {
        String normalized = (displayName == null || displayName.isBlank()) ? "ROLE" : displayName.trim();
        return status == RoleStatus.DISABLED ? DISABLED_PREFIX + normalized : normalized;
    }

    private PermissionDefinition parsePermission(String code) {
        String normalized = code == null ? "permission.manage" : code.trim();
        if (normalized.isBlank()) {
            normalized = "permission.manage";
        }
        int lastDot = normalized.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= normalized.length() - 1) {
            return new PermissionDefinition(normalized, "manage", "ORGANIZATION");
        }
        String resource = normalized.substring(0, lastDot);
        String action = normalized.substring(lastDot + 1);
        return new PermissionDefinition(resource, action, "ORGANIZATION");
    }

    private record PermissionDefinition(String resource, String action, String scope) {}
}
