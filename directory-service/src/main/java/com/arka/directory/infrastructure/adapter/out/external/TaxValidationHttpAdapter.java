package com.arka.directory.infrastructure.adapter.out.external;

import com.arka.directory.application.port.out.external.TaxValidationPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TaxValidationHttpAdapter implements TaxValidationPort {

    private final WebClient webClient;
    private final boolean enabled;
    private final String path;
    private final String authToken;
    private final Duration timeout;

    public TaxValidationHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.validation.base-url:http://validation-service:8090}") String baseUrl,
            @Value("${app.external.validation.enabled:false}") boolean enabled,
            @Value("${app.external.validation.tax-path:/api/v1/validation/tax-id}") String path,
            @Value("${app.external.validation.auth-token:}") String authToken,
            @Value("${app.external.validation.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
        this.path = path;
        this.authToken = authToken == null ? "" : authToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<Boolean> isTaxIdValid(String countryCode, String taxIdType, String taxId) {
        if (!enabled) {
            return Mono.just(true);
        }
        TaxValidationRequest request = new TaxValidationRequest(countryCode, taxIdType, taxId);
        return webClient
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .bodyValue(request)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response
                                .bodyToMono(ValidationResponse.class)
                                .map(ValidationResponse::valid)
                                .defaultIfEmpty(false);
                    }
                    int status = response.statusCode().value();
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, countryCode, taxIdType, taxId, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Tax validation service failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(
            int status,
            String countryCode,
            String taxIdType,
            String taxId,
            String body) {
        String normalizedCountry = countryCode == null ? "" : countryCode.trim();
        String normalizedTaxIdType = taxIdType == null ? "" : taxIdType.trim();
        String normalizedTaxId = taxId == null ? "" : taxId.trim();
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Tax validation request rejected (400). countryCode="
                            + normalizedCountry
                            + " taxIdType="
                            + normalizedTaxIdType
                            + " taxId="
                            + normalizedTaxId
                            + " body="
                            + body);
            case 401, 403 -> new SecurityException(
                    "Tax validation unauthorized/forbidden. status="
                            + status
                            + " countryCode="
                            + normalizedCountry
                            + " taxIdType="
                            + normalizedTaxIdType
                            + " taxId="
                            + normalizedTaxId
                            + " body="
                            + body);
            case 404 -> new IllegalStateException(
                    "Tax validation endpoint/resource not found (404). countryCode="
                            + normalizedCountry
                            + " taxIdType="
                            + normalizedTaxIdType
                            + " taxId="
                            + normalizedTaxId
                            + " body="
                            + body);
            case 409 -> new IllegalStateException(
                    "Tax validation conflict (409). countryCode="
                            + normalizedCountry
                            + " taxIdType="
                            + normalizedTaxIdType
                            + " taxId="
                            + normalizedTaxId
                            + " body="
                            + body);
            case 422 -> new IllegalStateException(
                    "Tax validation semantic error (422). countryCode="
                            + normalizedCountry
                            + " taxIdType="
                            + normalizedTaxIdType
                            + " taxId="
                            + normalizedTaxId
                            + " body="
                            + body);
            default -> new IllegalStateException(
                    "Tax validation client error. status="
                            + status
                            + " countryCode="
                            + normalizedCountry
                            + " taxIdType="
                            + normalizedTaxIdType
                            + " taxId="
                            + normalizedTaxId
                            + " body="
                            + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!authToken.isBlank()) {
            headers.setBearerAuth(authToken);
        }
    }

    private record TaxValidationRequest(String countryCode, String taxIdType, String taxId) {}

    private record ValidationResponse(boolean valid) {}
}
