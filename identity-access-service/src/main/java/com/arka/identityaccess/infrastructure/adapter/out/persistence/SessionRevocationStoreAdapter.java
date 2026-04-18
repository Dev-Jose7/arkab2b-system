package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.shared.port.RevocationStore;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SessionRevocationStoreAdapter implements RevocationStore {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final SessionPersistencePort sessionPersistencePort;
    private final ClockPort clockPort;

    public SessionRevocationStoreAdapter(
            SessionPersistencePort sessionPersistencePort,
            ClockPort clockPort) {
        this.sessionPersistencePort = sessionPersistencePort;
        this.clockPort = clockPort;
    }

    @Override
    public void revokeSession(SessionId sessionId, String reason, Instant occurredAt) {
        Instant effectiveAt = occurredAt == null ? clockPort.now() : occurredAt;
        sessionPersistencePort
                .findOptionalBySessionId(sessionId)
                .flatMap(session -> {
                    if (session.status() == SessionStatus.REVOKED) {
                        return reactor.core.publisher.Mono.empty();
                    }
                    return sessionPersistencePort.update(session.revoke(effectiveAt, reason));
                })
                .block(BLOCK_TIMEOUT);
    }

    @Override
    public void revokeAllByAccount(AccountId accountId, String reason, Instant occurredAt) {
        Instant effectiveAt = occurredAt == null ? clockPort.now() : occurredAt;
        sessionPersistencePort
                .revokeActiveSessionsByUserId(accountId, reason, effectiveAt)
                .then()
                .block(BLOCK_TIMEOUT);
    }

    @Override
    public boolean isSessionRevoked(SessionId sessionId) {
        return sessionPersistencePort
                .findOptionalBySessionId(sessionId)
                .map(session -> session.status() == SessionStatus.REVOKED)
                .defaultIfEmpty(Boolean.FALSE)
                .blockOptional(BLOCK_TIMEOUT)
                .orElse(Boolean.FALSE);
    }
}
