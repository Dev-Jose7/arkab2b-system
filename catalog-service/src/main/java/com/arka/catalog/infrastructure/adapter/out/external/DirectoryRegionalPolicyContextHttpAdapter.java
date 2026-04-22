package com.arka.catalog.infrastructure.adapter.out.external;

import com.arka.catalog.application.port.out.directory.RegionalPolicyContext;
import com.arka.catalog.application.port.out.directory.RegionalPolicyContextPort;
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
public class DirectoryRegionalPolicyContextHttpAdapter implements RegionalPolicyContextPort {

    private static final String DEFAULT_COUNTRY_POLICY_PATH =
            "/api/v1/internal/organizations/{organizationId}/country-policies/{countryCode}";

    private final WebClient webClient;
    private final String policyPath;
    private final String defaultCurrency;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public DirectoryRegionalPolicyContextHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service}") String baseUrl,
            @Value("${app.external.directory.country-policy-path:}") String policyPath,
            @Value("${app.external.directory.default-currency:COP}") String defaultCurrency,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.directory.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.directory.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.policyPath = policyPath == null || policyPath.isBlank() ? DEFAULT_COUNTRY_POLICY_PATH : policyPath;
        this.defaultCurrency = defaultCurrency == null ? "COP" : defaultCurrency.trim().toUpperCase();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public DirectoryRegionalPolicyContextHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String policyPath,
            String defaultCurrency,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, policyPath, defaultCurrency, timeoutMs, 2, 200L);
    }

    @Override
    public Mono<RegionalPolicyContext> resolveForOrganization(String organizationId, String countryCode) {
        String normalizedOrganizationId = organizationId == null ? "" : organizationId.trim();
        if (normalizedOrganizationId.isBlank()) {
            return Mono.error(new IllegalArgumentException("organizationId is required"));
        }
        String normalizedCountryCode = normalizeCountry(countryCode);
        return webClient
                .get()
                .uri(policyPath, normalizedOrganizationId, normalizedCountryCode)
                .accept(MediaType.APPLICATION_JSON)
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
                                .flatMap(body -> Mono.error(clientError(status, normalizedOrganizationId, normalizedCountryCode, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Regional policy context resolution failed status="
                                            + status
                                            + " organizationId="
                                            + normalizedOrganizationId
                                            + " countryCode="
                                            + normalizedCountryCode
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private RuntimeException clientError(int status, String organizationId, String countryCode, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Regional policy context request rejected (400). organizationId=" + organizationId + " countryCode="
                            + countryCode + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Regional policy context unauthorized/forbidden. status=" + status + " organizationId=" + organizationId
                            + " countryCode=" + countryCode + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Regional policy context conflict (409). organizationId=" + organizationId + " countryCode=" + countryCode
                            + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Regional policy context semantic error (422). organizationId=" + organizationId + " countryCode="
                            + countryCode + " body=" + body);
            default -> new IllegalStateException(
                    "Regional policy context client error. status=" + status + " organizationId=" + organizationId
                            + " countryCode=" + countryCode + " body=" + body);
        };
    }

    private String normalizeCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "CO";
        }
        return countryCode.trim().toUpperCase();
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

    private record CountryPolicyResponse(
            String policyId,
            String organizationId,
            String countryCode,
            String currencyCode,
            String status) {}
}
