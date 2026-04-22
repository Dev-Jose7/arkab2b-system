package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.RefreshSessionCommand;
import com.arka.identityaccess.application.mapper.command.RefreshSessionCommandAssembler;
import com.arka.identityaccess.application.mapper.result.TokenPairResultMapper;
import com.arka.identityaccess.application.port.in.RefreshSessionCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.application.result.TokenPairResult;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.service.SessionPolicy;
import com.arka.identityaccess.domain.session.service.TokenPolicy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RefreshSessionUseCase implements RefreshSessionCommandUseCase {

    private final SecurityRateLimitPort securityRateLimitPort;
    private final RefreshSessionCommandAssembler assembler;
    private final SessionPersistencePort sessionPersistencePort;
    private final ClockPort clockPort;
    private final SessionPolicy sessionPolicy;
    private final TokenPolicy tokenPolicy;
    private final JwtSigningPort jwtSigningPort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final TokenPairResultMapper resultMapper;
    private final UserPersistencePort userPersistencePort;

    public RefreshSessionUseCase(
            SecurityRateLimitPort securityRateLimitPort,
            RefreshSessionCommandAssembler assembler,
            SessionPersistencePort sessionPersistencePort,
            ClockPort clockPort,
            SessionPolicy sessionPolicy,
            TokenPolicy tokenPolicy,
            JwtSigningPort jwtSigningPort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            TokenPairResultMapper resultMapper,
            UserPersistencePort userPersistencePort) {
        this.securityRateLimitPort = securityRateLimitPort;
        this.assembler = assembler;
        this.sessionPersistencePort = sessionPersistencePort;
        this.clockPort = clockPort;
        this.sessionPolicy = sessionPolicy;
        this.tokenPolicy = tokenPolicy;
        this.jwtSigningPort = jwtSigningPort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.resultMapper = resultMapper;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Mono<TokenPairResult> handle(RefreshSessionCommand command) {
        ClientIp clientIp = ClientIp.of(command.ipAddress());
        return securityRateLimitPort
                .ensureRefreshAllowed(command.refreshToken(), clientIp)
                .then(Mono.defer(() -> assembler.verify(command)))
                .flatMap(verifiedToken -> sessionPersistencePort
                        .findActiveByRefreshJti(verifiedToken.refreshJti())
                        .map(session -> new RefreshedSessionContext(
                                session,
                                firstNonBlank(command.organizationId(), verifiedToken.organizationId()),
                                firstNonBlank(command.countryCode(), verifiedToken.countryCode()))))
                .map(context -> {
                    var now = clockPort.now();
                    sessionPolicy.ensureSessionCanRefresh(context.session(), now);
                    return new RefreshedSessionContext(
                            context.session().refresh(
                            now,
                            tokenPolicy.calculateAccessExpiry(now),
                            tokenPolicy.calculateRefreshExpiry(now),
                            sessionPolicy.shouldRotateRefreshTokenOnRefresh()),
                            context.organizationId(),
                            context.countryCode());
                })
                .flatMap(context -> sessionPersistencePort.update(context.session())
                        .map(savedSession -> new RefreshedSessionContext(
                                savedSession,
                                context.organizationId(),
                                context.countryCode())))
                .flatMap(context -> userPersistencePort.loadAuthorizationSnapshot(context.session().userId())
                        .flatMap(accessProfile -> Mono.zip(
                                        jwtSigningPort.signAccessToken(
                                                context.session(),
                                                accessProfile,
                                                context.organizationId(),
                                                context.countryCode()),
                                        jwtSigningPort.signRefreshToken(
                                                context.session(),
                                                context.organizationId(),
                                                context.countryCode()))
                                .flatMap(tokens -> securityAuditPort.recordSessionRefreshed(context.session())
                                        .then(publishDomainEvents(context.session().pullDomainEvents()))
                                        .thenReturn(resultMapper.toResult(context.session(), tokens.getT1(), tokens.getT2())))));
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }

    private Mono<Void> publishDomainEvents(Iterable<? extends DomainEvent> domainEvents) {
        return Flux.fromIterable(domainEvents).concatMap(outboxPersistencePort::store).then();
    }

    private record RefreshedSessionContext(
            com.arka.identityaccess.domain.session.aggregate.SessionAggregate session,
            String organizationId,
            String countryCode) {}
}
