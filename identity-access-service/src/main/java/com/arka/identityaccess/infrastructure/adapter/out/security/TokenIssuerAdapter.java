package com.arka.identityaccess.infrastructure.adapter.out.security;

import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.shared.port.TokenIssuer;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class TokenIssuerAdapter implements TokenIssuer {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final JwtSigningPort jwtSigningPort;

    public TokenIssuerAdapter(JwtSigningPort jwtSigningPort) {
        this.jwtSigningPort = jwtSigningPort;
    }

    @Override
    public String issueAccessToken(SessionAggregate session, AccessProfile accessProfile) {
        String token = jwtSigningPort.signAccessToken(session, accessProfile).block(BLOCK_TIMEOUT);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Access token issuer produced empty output");
        }
        return token;
    }

    @Override
    public String issueRefreshToken(SessionAggregate session) {
        String token = jwtSigningPort.signRefreshToken(session).block(BLOCK_TIMEOUT);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Refresh token issuer produced empty output");
        }
        return token;
    }
}
