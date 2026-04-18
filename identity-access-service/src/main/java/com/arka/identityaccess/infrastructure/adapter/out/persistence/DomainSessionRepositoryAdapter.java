package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.repository.SessionRepository;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper.SessionRowMapper;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveSessionRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DomainSessionRepositoryAdapter implements SessionRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final SessionPersistencePort sessionPersistencePort;
    private final ReactiveSessionRepository reactiveSessionRepository;
    private final SessionRowMapper sessionRowMapper;

    public DomainSessionRepositoryAdapter(
            SessionPersistencePort sessionPersistencePort,
            ReactiveSessionRepository reactiveSessionRepository,
            SessionRowMapper sessionRowMapper) {
        this.sessionPersistencePort = sessionPersistencePort;
        this.reactiveSessionRepository = reactiveSessionRepository;
        this.sessionRowMapper = sessionRowMapper;
    }

    @Override
    public SessionAggregate save(SessionAggregate session) {
        SessionAggregate persisted = sessionPersistencePort
                .findOptionalBySessionId(session.id())
                .flatMap(existing -> sessionPersistencePort.update(session))
                .switchIfEmpty(sessionPersistencePort.create(session))
                .block(BLOCK_TIMEOUT);
        if (persisted == null) {
            throw new IllegalStateException("Session persistence returned empty result");
        }
        return persisted;
    }

    @Override
    public Optional<SessionAggregate> findById(SessionId sessionId) {
        return sessionPersistencePort.findOptionalBySessionId(sessionId).blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public List<SessionAggregate> findActiveByAccountId(AccountId accountId) {
        return reactiveSessionRepository
                .findActiveByUserId(accountId.value())
                .map(sessionRowMapper::toAggregate)
                .collectList()
                .blockOptional(BLOCK_TIMEOUT)
                .orElseGet(List::of);
    }
}
