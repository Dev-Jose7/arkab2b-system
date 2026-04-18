package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.repository.AccountRepository;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DomainAccountRepositoryAdapter implements AccountRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);
    private static final Logger log = LoggerFactory.getLogger(DomainAccountRepositoryAdapter.class);

    private final UserPersistencePort userPersistencePort;
    private final RoleCode defaultRoleCode;
    private final String defaultAssignedBy;

    public DomainAccountRepositoryAdapter(
            UserPersistencePort userPersistencePort,
            @Value("${app.domain.account.default-role-code:BUYER}") String defaultRoleCode,
            @Value("${app.domain.account.default-assigned-by:SYSTEM_DOMAIN_REPOSITORY}") String defaultAssignedBy) {
        this.userPersistencePort = userPersistencePort;
        this.defaultRoleCode = RoleCode.of(defaultRoleCode);
        this.defaultAssignedBy = defaultAssignedBy == null || defaultAssignedBy.isBlank()
                ? "SYSTEM_DOMAIN_REPOSITORY"
                : defaultAssignedBy.trim();
    }

    @Override
    public AccountAggregate save(AccountAggregate account) {
        boolean exists = userPersistencePort.existsById(account.id()).blockOptional(BLOCK_TIMEOUT).orElse(Boolean.FALSE);
        Mono<AccountAggregate> persisted = exists
                ? userPersistencePort.loadById(account.id())
                : userPersistencePort.create(account, defaultRoleCode, defaultAssignedBy);

        AccountAggregate saved = persisted.block(BLOCK_TIMEOUT);
        if (saved == null) {
            throw new IllegalStateException("Account persistence returned empty result");
        }
        return saved;
    }

    @Override
    public Optional<AccountAggregate> findById(AccountId accountId) {
        return blockOptionalOrThrow(
                userPersistencePort.loadById(accountId),
                "findById",
                "Failed to load account by id from persistence. accountId=" + accountId.value());
    }

    @Override
    public Optional<AccountAggregate> findByEmail(EmailAddress emailAddress) {
        return blockOptionalOrThrow(
                userPersistencePort.loadForLogin(emailAddress),
                "findByEmail",
                "Failed to load account by email from persistence. email=" + emailAddress.normalized());
    }

    @Override
    public boolean existsByEmail(EmailAddress emailAddress) {
        return userPersistencePort.existsByEmail(emailAddress).blockOptional(BLOCK_TIMEOUT).orElse(Boolean.FALSE);
    }

    private <T> Optional<T> blockOptionalOrThrow(Mono<T> source, String operation, String errorMessage) {
        try {
            return source.blockOptional(BLOCK_TIMEOUT);
        } catch (RuntimeException exception) {
            log.error("Domain account repository operation failed. operation={}", operation, exception);
            throw new IllegalStateException(errorMessage, exception);
        }
    }
}
