package com.arka.reporting.infrastructure.adapter.out.external;

import com.arka.reporting.application.port.out.directory.RegionalPolicyPort;
import com.arka.reporting.application.port.out.directory.RegionalPolicyResolution;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class DirectoryRegionalPolicyHttpAdapter implements RegionalPolicyPort {

    private static final String DEFAULT_REGIONAL_CONTEXT_PATH =
            "/api/v1/internal/organizations/{organizationId}/regional-context/{countryCode}";

    private final WebClient webClient;
    private final String regionalContextPath;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public DirectoryRegionalPolicyHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service}") String baseUrl,
            @Value("${app.external.directory.regional-context-path:}") String regionalContextPath,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.directory.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.directory.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.regionalContextPath = regionalContextPath == null || regionalContextPath.isBlank()
                ? DEFAULT_REGIONAL_CONTEXT_PATH
                : regionalContextPath;
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public DirectoryRegionalPolicyHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String regionalContextPath,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, regionalContextPath, timeoutMs, 2, 200L);
    }

    @Override
    public Mono<RegionalPolicyResolution> resolveForOperation(String organizationId, String countryCode) {
        if (organizationId == null || organizationId.isBlank()) {
            return Mono.just(new RegionalPolicyResolution("", normalizeCountry(countryCode), false, ""));
        }
        String normalizedOrganization = organizationId.trim();
        String normalizedCountry = normalizeCountry(countryCode);
        return webClient
                .get()
                .uri(regionalContextPath, normalizedOrganization, normalizedCountry)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .map(body -> toResolution(normalizedOrganization, normalizedCountry, body));
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.just(new RegionalPolicyResolution(normalizedOrganization, normalizedCountry, false, ""));
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(
                                        status,
                                        normalizedOrganization,
                                        normalizedCountry,
                                        body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Regional policy resolution failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private RuntimeException clientError(int status, String organizationId, String countryCode, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Regional policy resolution request rejected (400). organizationId=" + organizationId + " countryCode="
                            + countryCode + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Regional policy resolution unauthorized/forbidden. status=" + status + " organizationId=" + organizationId
                            + " countryCode=" + countryCode + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Regional policy resolution conflict (409). organizationId=" + organizationId + " countryCode=" + countryCode
                            + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Regional policy resolution semantic error (422). organizationId=" + organizationId + " countryCode="
                            + countryCode + " body=" + body);
            default -> new IllegalStateException(
                    "Regional policy resolution client error. status=" + status + " organizationId=" + organizationId
                            + " countryCode=" + countryCode + " body=" + body);
        };
    }

    private RegionalPolicyResolution toResolution(String organizationId, String countryCode, JsonNode body) {
        JsonNode policy = body.path("countryPolicy");
        String status = text(policy, "status");
        String policyId = text(policy, "policyId");
        boolean active = "ACTIVE".equalsIgnoreCase(status) && policyId != null && !policyId.isBlank();
        return new RegionalPolicyResolution(organizationId, countryCode, active, active ? policyId : "");
    }

    private String normalizeCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "GLOBAL";
        }
        return countryCode.trim().toUpperCase();
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof TimeoutException
                || throwable instanceof WebClientRequestException
                || throwable instanceof TransientRemoteException;
    }

    private static final class TransientRemoteException extends RuntimeException {
        private TransientRemoteException(String message) {
            super(message);
        }
    }
}
