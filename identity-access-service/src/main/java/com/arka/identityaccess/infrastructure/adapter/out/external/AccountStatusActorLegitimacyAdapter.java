package com.arka.identityaccess.infrastructure.adapter.out.external;

import com.arka.identityaccess.application.port.out.external.ActorLegitimacyPort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountStatusActorLegitimacyAdapter implements ActorLegitimacyPort {

    private final UserPersistencePort userPersistencePort;

    public AccountStatusActorLegitimacyAdapter(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Mono<Boolean> isLegitimate(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            return Mono.just(false);
        }
        AccountId accountId;
        try {
            accountId = AccountId.of(actorUserId.trim());
        } catch (RuntimeException invalidId) {
            return Mono.just(false);
        }
        return userPersistencePort.loadStatus(accountId)
                .map(statusSnapshot -> "ACTIVE".equalsIgnoreCase(statusSnapshot.status()))
                .onErrorResume(error -> Mono.just(false))
                .defaultIfEmpty(false);
    }
}
