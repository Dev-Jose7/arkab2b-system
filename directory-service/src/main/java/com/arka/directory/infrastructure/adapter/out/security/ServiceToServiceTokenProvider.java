package com.arka.directory.infrastructure.adapter.out.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ServiceToServiceTokenProvider {

    private final WebClient tokenClient;
    private final boolean enabled;
    private final String clientId;
    private final String clientSecret;
    private final String audience;
    private final List<String> scopes;
    private final String tokenPath;
    private final Duration timeout;
    private final Duration refreshSkew;
    private volatile CachedToken cachedToken;

    public ServiceToServiceTokenProvider(
            @Qualifier("loadBalancedNoAuthWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.security.s2s.enabled:true}") boolean enabled,
            @Value("${app.security.s2s.client-id:${spring.application.name}}") String clientId,
            @Value("${app.security.s2s.client-secret:}") String clientSecret,
            @Value("${app.security.s2s.audience:arka-b2b}") String audience,
            @Value("${app.security.s2s.scopes:}") List<String> scopes,
            @Value("${app.security.s2s.identity-base-url:http://identity-access-service}") String identityBaseUrl,
            @Value("${app.security.s2s.token-path:/api/v1/internal/auth/service-token}") String tokenPath,
            @Value("${app.security.s2s.timeout-ms:2000}") long timeoutMs,
            @Value("${app.security.s2s.refresh-skew-seconds:30}") long refreshSkewSeconds) {
        this.enabled = enabled;
        this.clientId = normalize(clientId);
        this.clientSecret = normalize(clientSecret);
        this.audience = normalizeOrDefault(audience, "arka-b2b");
        this.scopes = scopes == null
                ? List.of()
                : scopes.stream()
                        .filter(scope -> scope != null && !scope.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList();
        this.tokenPath = normalizeOrDefault(tokenPath, "/api/v1/internal/auth/service-token");
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.refreshSkew = Duration.ofSeconds(Math.max(5L, refreshSkewSeconds));
        this.tokenClient = webClientBuilder.baseUrl(normalizeOrDefault(identityBaseUrl, "http://identity-access-service")).build();
    }

    public Mono<String> currentToken() {
        if (!enabled) {
            return Mono.error(new IllegalStateException("service-to-service token provider is disabled"));
        }
        if (clientId.isBlank() || clientSecret.isBlank()) {
            return Mono.error(new IllegalStateException("service-to-service credentials are not configured"));
        }

        CachedToken snapshot = cachedToken;
        Instant now = Instant.now();
        if (snapshot != null && snapshot.expiresAt().isAfter(now.plus(refreshSkew))) {
            return Mono.just(snapshot.token());
        }
        return requestNewToken()
                .map(token -> {
                    this.cachedToken = token;
                    return token.token();
                });
    }

    private Mono<CachedToken> requestNewToken() {
        ServiceTokenIssueRequest request = new ServiceTokenIssueRequest(
                clientId,
                clientSecret,
                audience,
                scopes,
                null,
                null);

        return tokenClient
                .post()
                .uri(tokenPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ServiceTokenIssueResponse.class)
                .timeout(timeout)
                .map(this::toCachedToken);
    }

    private CachedToken toCachedToken(ServiceTokenIssueResponse response) {
        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("identity service returned an empty service token");
        }
        Instant now = Instant.now();
        Instant expiresAt = response.expiresAt();
        if (expiresAt == null) {
            long expiresIn = response.expiresIn() == null ? 0L : response.expiresIn();
            expiresAt = now.plusSeconds(Math.max(30L, expiresIn));
        }
        return new CachedToken(response.accessToken().trim(), expiresAt);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeOrDefault(String value, String fallback) {
        String normalized = normalize(value);
        return normalized.isBlank() ? fallback : normalized;
    }

    private record ServiceTokenIssueRequest(
            String clientId,
            String clientSecret,
            String audience,
            List<String> scopes,
            String organizationId,

            String countryCode) {}

    private record ServiceTokenIssueResponse(
            String accessToken,
            String tokenType,
            Long expiresIn,
            String scope,
            String audience,
            String issuer,
            String subject,
            String clientId,
            Instant issuedAt,
            Instant expiresAt) {}

    private record CachedToken(String token, Instant expiresAt) {}
}
