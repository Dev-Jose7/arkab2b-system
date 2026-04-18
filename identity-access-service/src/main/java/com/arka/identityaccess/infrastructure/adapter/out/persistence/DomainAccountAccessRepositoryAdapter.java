package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.domain.access.aggregate.AccountAccessAggregate;
import com.arka.identityaccess.domain.access.entity.AccessAssignment;
import com.arka.identityaccess.domain.access.enumtype.AccessAssignmentStatus;
import com.arka.identityaccess.domain.access.repository.AccountAccessRepository;
import com.arka.identityaccess.domain.access.valueobject.AccessAssignmentId;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRoleRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRoleRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DomainAccountAccessRepositoryAdapter implements AccountAccessRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final ReactiveUserRoleRepository userRoleRepository;

    public DomainAccountAccessRepositoryAdapter(ReactiveUserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public AccountAccessAggregate save(AccountAccessAggregate accountAccess) {
        AccountId accountId = accountAccess.accountId();
        List<UserRoleRow> existingRows = userRoleRepository
                .findAllAssignmentsByUserId(accountId.value())
                .collectList()
                .blockOptional(BLOCK_TIMEOUT)
                .orElseGet(List::of);

        Map<String, UserRoleRow> existingByRoleId = existingRows.stream().collect(Collectors.toMap(UserRoleRow::roleId, Function.identity(), (left, right) -> left));
        Map<String, AccessAssignment> desiredByRoleId = accountAccess.assignments().stream().collect(Collectors.toMap(assignment -> assignment.roleId().value(), Function.identity(), (left, right) -> left));

        Instant now = Instant.now();

        for (AccessAssignment assignment : accountAccess.assignments()) {
            String roleId = assignment.roleId().value();
            UserRoleRow current = existingByRoleId.get(roleId);
            if (assignment.isActive()) {
                if (current == null) {
                    userRoleRepository
                            .insertIgnoreIfExists(
                                    assignment.id().value(),
                                    accountId.value(),
                                    roleId,
                                    "ACTIVE",
                                    assignment.assignedBy(),
                                    assignment.assignedAt(),
                                    assignment.assignedAt(),
                                    now)
                            .block(BLOCK_TIMEOUT);
                } else if (!"ACTIVE".equalsIgnoreCase(current.status())) {
                    userRoleRepository
                            .updateAssignmentStatus(accountId.value(), roleId, "ACTIVE", now)
                            .block(BLOCK_TIMEOUT);
                }
            } else if (current != null && "ACTIVE".equalsIgnoreCase(current.status())) {
                userRoleRepository
                        .updateAssignmentStatus(accountId.value(), roleId, "REVOKED", now)
                        .block(BLOCK_TIMEOUT);
            }
        }

        for (UserRoleRow current : existingRows) {
            if (!"ACTIVE".equalsIgnoreCase(current.status())) {
                continue;
            }
            if (!desiredByRoleId.containsKey(current.roleId())) {
                userRoleRepository
                        .updateAssignmentStatus(accountId.value(), current.roleId(), "REVOKED", now)
                        .block(BLOCK_TIMEOUT);
            }
        }

        return findByAccountId(accountId).orElseGet(() -> AccountAccessAggregate.empty(accountId));
    }

    @Override
    public Optional<AccountAccessAggregate> findByAccountId(AccountId accountId) {
        List<UserRoleRow> rows = userRoleRepository
                .findAllAssignmentsByUserId(accountId.value())
                .collectList()
                .blockOptional(BLOCK_TIMEOUT)
                .orElseGet(List::of);

        if (rows.isEmpty()) {
            return Optional.of(AccountAccessAggregate.empty(accountId));
        }

        List<AccessAssignment> assignments = new ArrayList<>(rows.size());
        for (UserRoleRow row : rows) {
            assignments.add(AccessAssignment.rehydrate(
                    AccessAssignmentId.of(row.assignmentId() == null || row.assignmentId().isBlank() ? UUID.randomUUID().toString() : row.assignmentId()),
                    accountId,
                    RoleId.of(row.roleId()),
                    toAssignmentStatus(row.status()),
                    row.assignedBy() == null || row.assignedBy().isBlank() ? "SYSTEM" : row.assignedBy(),
                    row.assignedAt() == null ? Instant.now() : row.assignedAt(),
                    null,
                    null));
        }
        return Optional.of(AccountAccessAggregate.rehydrate(accountId, assignments));
    }

    private AccessAssignmentStatus toAssignmentStatus(String status) {
        return "ACTIVE".equalsIgnoreCase(status) ? AccessAssignmentStatus.ASSIGNED : AccessAssignmentStatus.REVOKED;
    }
}
