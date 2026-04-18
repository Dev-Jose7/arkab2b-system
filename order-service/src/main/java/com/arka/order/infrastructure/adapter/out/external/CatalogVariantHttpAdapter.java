package com.arka.order.infrastructure.adapter.out.external;

import com.arka.order.application.port.out.external.CatalogVariantPort;
import com.arka.order.application.port.out.external.CatalogVariantSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CatalogVariantHttpAdapter implements CatalogVariantPort {

    private final WebClient webClient;
    private final String path;
    private final String serviceToken;
    private final String defaultCurrency;
    private final String defaultPriceType;
    private final Duration timeout;

    public CatalogVariantHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.catalog.base-url:http://catalog-service:8082}") String baseUrl,
            @Value("${app.external.catalog.variant-resolution-path:/api/v1/catalog/checkout/variant-resolution}") String path,
            @Value("${app.external.catalog.service-token:}") String serviceToken,
            @Value("${app.external.catalog.default-currency:COP}") String defaultCurrency,
            @Value("${app.external.catalog.default-price-type:BASE}") String defaultPriceType,
            @Value("${app.external.catalog.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.path = path;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.defaultCurrency = defaultCurrency == null || defaultCurrency.isBlank()
                ? "COP"
                : defaultCurrency.trim().toUpperCase();
        this.defaultPriceType = defaultPriceType == null || defaultPriceType.isBlank()
                ? "BASE"
                : defaultPriceType.trim().toUpperCase();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<CatalogVariantSnapshot> resolveVariant(String tenantId, String variantId, String sku) {
        if (sku == null || sku.isBlank()) {
            return Mono.empty();
        }
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("sku", sku.trim())
                        .queryParam("currency", defaultCurrency)
                        .queryParam("priceType", defaultPriceType)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class).map(this::toSnapshot);
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.empty();
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, sku, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Catalog variant resolution failed status="
                                            + response.statusCode().value()
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private CatalogVariantSnapshot toSnapshot(JsonNode node) {
        String resolvedVariantId = text(node, "variantId");
        String resolvedSku = text(node, "sku");
        BigDecimal amount = decimal(node, "amount");
        String currency = text(node, "currency");
        return new CatalogVariantSnapshot(
                resolvedVariantId,
                resolvedSku,
                amount,
                currency == null || currency.isBlank() ? defaultCurrency : currency,
                true);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }

    private RuntimeException clientError(int status, String sku, String body) {
        String normalizedSku = sku == null ? "" : sku.trim();
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Catalog variant resolution request rejected (400). sku=" + normalizedSku + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Catalog variant resolution unauthorized/forbidden. status=" + status + " sku=" + normalizedSku
                            + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Catalog variant resolution conflict (409). sku=" + normalizedSku + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Catalog variant resolution semantic error (422). sku=" + normalizedSku + " body=" + body);
            default -> new IllegalStateException(
                    "Catalog variant resolution client error. status=" + status + " sku=" + normalizedSku + " body="
                            + body);
        };
    }
}
