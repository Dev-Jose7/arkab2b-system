package com.arka.order.infrastructure.adapter.out.external;

import com.arka.order.application.port.out.directory.DirectoryCheckoutContext;
import com.arka.order.application.port.out.directory.DirectoryCheckoutPort;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class DirectoryCheckoutHttpAdapter implements DirectoryCheckoutPort {

    private final WebClient webClient;
    private final String path;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public DirectoryCheckoutHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service}") String baseUrl,
            @Value("${app.external.directory.checkout-resolution-path:/api/v1/internal/organizations/{organizationId}/addresses/{addressId}/checkout-resolution}") String path,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.directory.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.directory.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.path = path;
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public DirectoryCheckoutHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String path,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, path, timeoutMs, 2, 200L);
    }

    @Override
    public Mono<DirectoryCheckoutContext> resolveCheckoutContext(
            String organizationId,

            String addressId,
            String countryCode) {
        if (organizationId == null || organizationId.isBlank() || addressId == null || addressId.isBlank()) {
            return Mono.error(new IllegalArgumentException("organizationId and addressId are required"));
        }
        String normalizedCountry = countryCode == null || countryCode.isBlank()
                ? "CO"
                : countryCode.trim().toUpperCase();

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("countryCode", normalizedCountry)
                        .build(organizationId.trim(), addressId.trim()))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response
                                .bodyToMono(JsonNode.class)
                                .map(body -> toContext(organizationId.trim(), addressId.trim(), normalizedCountry, body));
                    }
                    int status = response.statusCode().value();
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(
                                        status,
                                        organizationId.trim(),
                                        addressId.trim(),
                                        normalizedCountry,
                                        body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Directory checkout resolution failed status="
                                            + status
                                            + " organizationId="
                                            + organizationId.trim()
                                            + " addressId="
                                            + addressId.trim()
                                            + " countryCode="
                                            + normalizedCountry
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private RuntimeException clientError(
            int status,
            String organizationId,
            String addressId,
            String countryCode,
            String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Directory checkout request rejected (400). organizationId="
                            + organizationId
                            + " addressId="
                            + addressId
                            + " countryCode="
                            + countryCode
                            + " body="
                            + body);
            case 401, 403 -> new SecurityException(
                    "Directory checkout unauthorized/forbidden. status="
                            + status
                            + " organizationId="
                            + organizationId
                            + " addressId="
                            + addressId
                            + " countryCode="
                            + countryCode
                            + " body="
                            + body);
            case 404 -> new IllegalArgumentException(
                    "Directory checkout context not found (404). organizationId="
                            + organizationId
                            + " addressId="
                            + addressId
                            + " countryCode="
                            + countryCode
                            + " body="
                            + body);
            case 409 -> new IllegalStateException(
                    "Directory checkout conflict (409). organizationId="
                            + organizationId
                            + " addressId="
                            + addressId
                            + " countryCode="
                            + countryCode
                            + " body="
                            + body);
            case 422 -> new IllegalStateException(
                    "Directory checkout semantic error (422). organizationId="
                            + organizationId
                            + " addressId="
                            + addressId
                            + " countryCode="
                            + countryCode
                            + " body="
                            + body);
            default -> new IllegalStateException(
                    "Directory checkout client error. status="
                            + status
                            + " organizationId="
                            + organizationId
                            + " addressId="
                            + addressId
                            + " countryCode="
                            + countryCode
                            + " body="
                            + body);
        };
    }

    private DirectoryCheckoutContext toContext(
            String organizationId,
            String addressId,
            String requestedCountryCode,
            JsonNode body) {
        JsonNode address = body.path("address");
        JsonNode policy = body.path("countryPolicy");
        String country = text(address, "countryCode");
        if (country == null || country.isBlank()) {
            country = requestedCountryCode;
        }
        String currency = text(policy, "currencyCode");
        if (currency == null || currency.isBlank()) {
            currency = "COP";
        }
        long version = policy.path("policyVersion").asLong(0L);
        boolean policyActive = "ACTIVE".equalsIgnoreCase(text(policy, "status"));
        String resolutionStatus = text(body, "resolutionStatus");
        boolean addressValid = "RESOLVED".equalsIgnoreCase(resolutionStatus)
                || "VALID".equalsIgnoreCase(text(address, "validationStatus"));
        return new DirectoryCheckoutContext(
                organizationId,
                addressId,
                country.toUpperCase(),
                version,
                currency.toUpperCase(),
                policyActive,
                addressValid);
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
