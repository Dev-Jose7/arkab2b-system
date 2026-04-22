package com.arka.identityaccess.application.port.out.security;

import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface JwtSigningPort {

    Mono<String> signAccessToken(
            SessionAggregate session,
            AccessProfile accessProfile,
            String organizationId,
            String countryCode);

    Mono<String> signRefreshToken(
            SessionAggregate session,
            String organizationId,
            String countryCode);
}
