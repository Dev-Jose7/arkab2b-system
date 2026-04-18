package com.arka.reporting.infrastructure.adapter.out.external;

import com.arka.reporting.application.port.out.directory.RegionalPolicyPort;
import com.arka.reporting.application.port.out.directory.RegionalPolicyResolution;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DirectoryRegionalPolicyHttpAdapter implements RegionalPolicyPort {

    private static final String DEFAULT_REGIONAL_CONTEXT_PATH =
            "/api/v1/organizations/{organizationId}/regional-context/{countryCode}";

    private final WebClient webClient;
    private final String regionalContextPath;
    private final String serviceToken;
    private final Duration timeout;

    public DirectoryRegionalPolicyHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service:8080}") String baseUrl,
            @Value("${app.external.directory.regional-context-path:}") String regionalContextPath,
            @Value("${app.external.directory.service-token:}") String serviceToken,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.regionalContextPath = regionalContextPath == null || regionalContextPath.isBlank()
                ? DEFAULT_REGIONAL_CONTEXT_PATH
                : regionalContextPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<RegionalPolicyResolution> resolveForOperation(String tenantId, String countryCode) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.just(new RegionalPolicyResolution("", normalizeCountry(countryCode), false, ""));
        }
        String normalizedTenant = tenantId.trim();
        String normalizedCountry = normalizeCountry(countryCode);
        return webClient
                .get()
                .uri(regionalContextPath, normalizedTenant, normalizedCountry)
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .map(body -> toResolution(normalizedTenant, normalizedCountry, body));
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.just(new RegionalPolicyResolution(normalizedTenant, normalizedCountry, false, ""));
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(
                                        status,
                                        normalizedTenant,
                                        normalizedCountry,
                                        body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Regional policy resolution failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String tenantId, String countryCode, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Regional policy resolution request rejected (400). tenantId=" + tenantId + " countryCode="
                            + countryCode + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Regional policy resolution unauthorized/forbidden. status=" + status + " tenantId=" + tenantId
                            + " countryCode=" + countryCode + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Regional policy resolution conflict (409). tenantId=" + tenantId + " countryCode=" + countryCode
                            + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Regional policy resolution semantic error (422). tenantId=" + tenantId + " countryCode="
                            + countryCode + " body=" + body);
            default -> new IllegalStateException(
                    "Regional policy resolution client error. status=" + status + " tenantId=" + tenantId
                            + " countryCode=" + countryCode + " body=" + body);
        };
    }

    private RegionalPolicyResolution toResolution(String tenantId, String countryCode, JsonNode body) {
        JsonNode policy = body.path("countryPolicy");
        String status = text(policy, "status");
        String policyId = text(policy, "policyId");
        boolean active = "ACTIVE".equalsIgnoreCase(status) && policyId != null && !policyId.isBlank();
        return new RegionalPolicyResolution(tenantId, countryCode, active, active ? policyId : "");
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

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }
}
