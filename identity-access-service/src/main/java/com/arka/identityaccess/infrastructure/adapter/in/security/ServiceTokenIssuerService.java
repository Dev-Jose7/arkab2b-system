package com.arka.identityaccess.infrastructure.adapter.in.security;

import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.ServiceTokenIssueRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.ServiceTokenResponse;
import com.arka.identityaccess.infrastructure.config.ServiceClientRegistryProperties;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class ServiceTokenIssuerService {

    private final JwtSigningPort jwtSigningPort;
    private final ServiceClientRegistryProperties registry;
    private final String issuer;

    public ServiceTokenIssuerService(
            JwtSigningPort jwtSigningPort,
            ServiceClientRegistryProperties registry,
            @Value("${app.security.jwt.issuer:identity-access-service}") String issuer) {
        this.jwtSigningPort = jwtSigningPort;
        this.registry = registry;
        this.issuer = issuer;
    }

    public Mono<ServiceTokenResponse> issue(ServiceTokenIssueRequest request) {
        if (!registry.isEnabled()) {
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "service token issuance is disabled"));
        }
        ServiceClientRegistryProperties.Client client = registry.resolvedClient(request.clientId());
        if (client == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid service client credentials"));
        }

        String expectedSecret = normalize(client.getClientSecret());
        String providedSecret = normalize(request.clientSecret());
        if (expectedSecret.isBlank() || !expectedSecret.equals(providedSecret)) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid service client credentials"));
        }

        String audience = normalize(request.audience());
        Set<String> allowedAudiences = client.normalizedAudiences();
        if (audience.isBlank()) {
            audience = allowedAudiences.stream().findFirst().orElse("arka-b2b");
        }
        if (!allowedAudiences.isEmpty() && !allowedAudiences.contains(audience)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "requested audience is not allowed"));
        }
        final String tokenAudience = audience;

        Set<String> allowedScopes = client.normalizedScopes();
        Set<String> requestedScopes = normalizeValues(request.scopes());
        Set<String> scopes = requestedScopes.isEmpty() ? allowedScopes : requestedScopes;
        if (!allowedScopes.containsAll(scopes)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "requested scopes are not allowed"));
        }

        long ttlSeconds = client.getTokenTtlSeconds() != null && client.getTokenTtlSeconds() > 0
                ? client.getTokenTtlSeconds()
                : Math.max(60L, registry.getDefaultTokenTtlSeconds());

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(ttlSeconds);

        String organizationId = normalizeNullable(request.organizationId());

        JwtSigningPort.ServiceTokenClaims claims = new JwtSigningPort.ServiceTokenClaims(
                client.getClientId(),
                scopes,
                client.normalizedRoles(),
                tokenAudience,
                organizationId,
                normalizeNullable(request.countryCode()),
                ttlSeconds);

        return jwtSigningPort.signServiceToken(claims)
                .map(token -> new ServiceTokenResponse(
                        token,
                        "Bearer",
                        ttlSeconds,
                        scopes.stream().sorted().collect(Collectors.joining(" ")),
                        tokenAudience,
                        issuer,
                        "svc:" + client.getClientId(),
                        client.getClientId(),
                        issuedAt,
                        expiresAt));
    }

    private Set<String> normalizeValues(Iterable<String> values) {
        if (values == null) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            normalized.add(value.trim());
        }
        return Set.copyOf(normalized);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() ? null : normalized;
    }
}
