package com.arka.directory.infrastructure.adapter.out.external;

import com.arka.directory.application.port.out.external.GeoValidationPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GeoValidationHttpAdapter implements GeoValidationPort {

    private final WebClient webClient;
    private final boolean enabled;
    private final String path;
    private final String authToken;
    private final Duration timeout;

    public GeoValidationHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.validation.base-url:http://validation-service:8090}") String baseUrl,
            @Value("${app.external.validation.enabled:false}") boolean enabled,
            @Value("${app.external.validation.geo-path:/api/v1/validation/geo/address}") String path,
            @Value("${app.external.validation.auth-token:}") String authToken,
            @Value("${app.external.validation.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
        this.path = path;
        this.authToken = authToken == null ? "" : authToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<Boolean> isAddressValid(String countryCode, String city, String postalCode, String line1) {
        if (!enabled) {
            return Mono.just(true);
        }
        GeoValidationRequest request = new GeoValidationRequest(countryCode, city, postalCode, line1);
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
                                .flatMap(body -> Mono.error(clientError(status, countryCode, city, postalCode, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Geo validation service failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String countryCode, String city, String postalCode, String body) {
        String normalizedCountry = countryCode == null ? "" : countryCode.trim();
        String normalizedCity = city == null ? "" : city.trim();
        String normalizedPostalCode = postalCode == null ? "" : postalCode.trim();
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Geo validation request rejected (400). countryCode="
                            + normalizedCountry
                            + " city="
                            + normalizedCity
                            + " postalCode="
                            + normalizedPostalCode
                            + " body="
                            + body);
            case 401, 403 -> new SecurityException(
                    "Geo validation unauthorized/forbidden. status="
                            + status
                            + " countryCode="
                            + normalizedCountry
                            + " city="
                            + normalizedCity
                            + " postalCode="
                            + normalizedPostalCode
                            + " body="
                            + body);
            case 404 -> new IllegalStateException(
                    "Geo validation endpoint/resource not found (404). countryCode="
                            + normalizedCountry
                            + " city="
                            + normalizedCity
                            + " postalCode="
                            + normalizedPostalCode
                            + " body="
                            + body);
            case 409 -> new IllegalStateException(
                    "Geo validation conflict (409). countryCode="
                            + normalizedCountry
                            + " city="
                            + normalizedCity
                            + " postalCode="
                            + normalizedPostalCode
                            + " body="
                            + body);
            case 422 -> new IllegalStateException(
                    "Geo validation semantic error (422). countryCode="
                            + normalizedCountry
                            + " city="
                            + normalizedCity
                            + " postalCode="
                            + normalizedPostalCode
                            + " body="
                            + body);
            default -> new IllegalStateException(
                    "Geo validation client error. status="
                            + status
                            + " countryCode="
                            + normalizedCountry
                            + " city="
                            + normalizedCity
                            + " postalCode="
                            + normalizedPostalCode
                            + " body="
                            + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!authToken.isBlank()) {
            headers.setBearerAuth(authToken);
        }
    }

    private record GeoValidationRequest(String countryCode, String city, String postalCode, String line1) {}

    private record ValidationResponse(boolean valid) {}
}
