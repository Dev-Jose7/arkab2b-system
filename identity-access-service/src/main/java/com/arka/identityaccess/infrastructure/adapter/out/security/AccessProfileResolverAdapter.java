package com.arka.identityaccess.infrastructure.adapter.out.security;

import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.port.AccessProfileResolver;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class AccessProfileResolverAdapter implements AccessProfileResolver {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final UserPersistencePort userPersistencePort;

    public AccessProfileResolverAdapter(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public AccessProfile resolve(AccountId accountId) {
        AccessProfile accessProfile = userPersistencePort.loadAuthorizationSnapshot(accountId).block(BLOCK_TIMEOUT);
        if (accessProfile == null) {
            throw new IllegalStateException("Unable to resolve access profile for account=" + accountId.value());
        }
        return accessProfile;
    }
}
