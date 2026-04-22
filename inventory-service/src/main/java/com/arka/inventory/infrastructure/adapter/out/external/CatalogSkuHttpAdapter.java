package com.arka.inventory.infrastructure.adapter.out.external;

import com.arka.inventory.application.port.out.external.CatalogSkuPort;
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
public class CatalogSkuHttpAdapter implements CatalogSkuPort {

    private final WebClient webClient;
    private final String path;
    private final String currency;
    private final String priceType;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public CatalogSkuHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.catalog.base-url:http://catalog-service}") String baseUrl,
            @Value("${app.external.catalog.variant-resolution-path:/api/v1/internal/catalog/checkout/variant-resolution}") String path,
            @Value("${app.external.catalog.default-currency:COP}") String currency,
            @Value("${app.external.catalog.default-price-type:BASE}") String priceType,
            @Value("${app.external.catalog.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.catalog.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.catalog.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.path = path;
        this.currency = currency == null || currency.isBlank() ? "COP" : currency.trim().toUpperCase();
        this.priceType = priceType == null || priceType.isBlank() ? "BASE" : priceType.trim().toUpperCase();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public CatalogSkuHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String path,
            String currency,
            String priceType,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, path, currency, priceType, timeoutMs, 2, 200L);
    }

    @Override
    public Mono<Boolean> existsSellableSku(String organizationId, String sku) {
        if (organizationId == null || organizationId.isBlank() || sku == null || sku.isBlank()) {
            return Mono.just(false);
        }
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("organizationId", organizationId.trim())
                        .queryParam("sku", sku.trim())
                        .queryParam("currency", currency)
                        .queryParam("priceType", priceType)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
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
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Catalog SKU validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
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
