package com.arka.identityaccess.application.port.out.security;

import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface JwtSigningPort {

    Mono<String> signAccessToken(SessionAggregate session, AccessProfile accessProfile);

    Mono<String> signRefreshToken(SessionAggregate session);

    Mono<String> signServiceToken(ServiceTokenClaims claims);

    record ServiceTokenClaims(
            String clientId,
            Set<String> scopes,
            Set<String> roles,
            String audience,
            String organizationId,
            String countryCode,
            long ttlSeconds) {}
}
