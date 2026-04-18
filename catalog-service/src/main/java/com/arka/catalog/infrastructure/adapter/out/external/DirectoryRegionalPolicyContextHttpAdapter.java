package com.arka.catalog.infrastructure.adapter.out.external;

import com.arka.catalog.application.port.out.directory.RegionalPolicyContext;
import com.arka.catalog.application.port.out.directory.RegionalPolicyContextPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DirectoryRegionalPolicyContextHttpAdapter implements RegionalPolicyContextPort {

    private static final String DEFAULT_COUNTRY_POLICY_PATH =
            "/api/v1/organizations/{organizationId}/country-policies/{countryCode}";

    private final WebClient webClient;
    private final String policyPath;
    private final String serviceToken;
    private final String defaultCurrency;
    private final Duration timeout;

    public DirectoryRegionalPolicyContextHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service:8080}") String baseUrl,
            @Value("${app.external.directory.country-policy-path:}") String policyPath,
            @Value("${app.external.directory.service-token:}") String serviceToken,
            @Value("${app.external.directory.default-currency:COP}") String defaultCurrency,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.policyPath = policyPath == null || policyPath.isBlank() ? DEFAULT_COUNTRY_POLICY_PATH : policyPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.defaultCurrency = defaultCurrency == null ? "COP" : defaultCurrency.trim().toUpperCase();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<RegionalPolicyContext> resolveForTenant(String tenantId, String countryCode) {
        String normalizedTenantId = tenantId == null ? "" : tenantId.trim();
        if (normalizedTenantId.isBlank()) {
            return Mono.error(new IllegalArgumentException("tenantId is required"));
        }
        String normalizedCountryCode = normalizeCountry(countryCode);
        return webClient
                .get()
                .uri(policyPath, normalizedTenantId, normalizedCountryCode)
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response
                                .bodyToMono(CountryPolicyResponse.class)
                                .map(payload -> new RegionalPolicyContext(
                                        payload.policyId(),
                                        normalizedCountryCode,
                                        payload.currencyCode() == null || payload.currencyCode().isBlank()
                                                ? defaultCurrency
                                                : payload.currencyCode().trim().toUpperCase()));
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.empty();
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, normalizedTenantId, normalizedCountryCode, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Regional policy context resolution failed status="
                                            + status
                                            + " tenantId="
                                            + normalizedTenantId
                                            + " countryCode="
                                            + normalizedCountryCode
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String tenantId, String countryCode, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Regional policy context request rejected (400). tenantId=" + tenantId + " countryCode="
                            + countryCode + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Regional policy context unauthorized/forbidden. status=" + status + " tenantId=" + tenantId
                            + " countryCode=" + countryCode + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Regional policy context conflict (409). tenantId=" + tenantId + " countryCode=" + countryCode
                            + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Regional policy context semantic error (422). tenantId=" + tenantId + " countryCode="
                            + countryCode + " body=" + body);
            default -> new IllegalStateException(
                    "Regional policy context client error. status=" + status + " tenantId=" + tenantId
                            + " countryCode=" + countryCode + " body=" + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }

    private String normalizeCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "CO";
        }
        return countryCode.trim().toUpperCase();
    }

    private record CountryPolicyResponse(
            String policyId,
            String organizationId,
            String countryCode,
            String currencyCode,
            String status) {}
}
