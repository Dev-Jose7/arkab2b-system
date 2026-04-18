package com.arka.identityaccess.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record ServiceTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String scope,
        String audience,
        String issuer,
        String subject,
        String clientId,
        Instant issuedAt,
        Instant expiresAt) {}
