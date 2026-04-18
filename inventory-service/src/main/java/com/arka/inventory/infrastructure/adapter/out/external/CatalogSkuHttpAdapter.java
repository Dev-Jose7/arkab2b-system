package com.arka.inventory.infrastructure.adapter.out.external;

import com.arka.inventory.application.port.out.external.CatalogSkuPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CatalogSkuHttpAdapter implements CatalogSkuPort {

    private final WebClient webClient;
    private final String path;
    private final String serviceToken;
    private final String currency;
    private final String priceType;
    private final Duration timeout;

    public CatalogSkuHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.catalog.base-url:http://catalog-service:8082}") String baseUrl,
            @Value("${app.external.catalog.variant-resolution-path:/api/v1/catalog/checkout/variant-resolution}") String path,
            @Value("${app.external.catalog.service-token:}") String serviceToken,
            @Value("${app.external.catalog.default-currency:COP}") String currency,
            @Value("${app.external.catalog.default-price-type:BASE}") String priceType,
            @Value("${app.external.catalog.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.path = path;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.currency = currency == null || currency.isBlank() ? "COP" : currency.trim().toUpperCase();
        this.priceType = priceType == null || priceType.isBlank() ? "BASE" : priceType.trim().toUpperCase();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<Boolean> existsSellableSku(String tenantId, String sku) {
        if (tenantId == null || tenantId.isBlank() || sku == null || sku.isBlank()) {
            return Mono.just(false);
        }
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("sku", sku.trim())
                        .queryParam("currency", currency)
                        .queryParam("priceType", priceType)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just(true);
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.just(false);
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, sku, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Catalog SKU validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String sku, String body) {
        String normalizedSku = sku == null ? "" : sku.trim();
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Catalog SKU validation request rejected (400). sku=" + normalizedSku + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Catalog SKU validation unauthorized/forbidden. status=" + status + " sku=" + normalizedSku
                            + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Catalog SKU validation conflict (409). sku=" + normalizedSku + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Catalog SKU validation semantic error (422). sku=" + normalizedSku + " body=" + body);
            default -> new IllegalStateException(
                    "Catalog SKU validation client error. status=" + status + " sku=" + normalizedSku + " body="
                            + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }
}
