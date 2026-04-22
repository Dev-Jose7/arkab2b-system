package com.arka.catalog.infrastructure.adapter.out.external;

import com.arka.catalog.infrastructure.adapter.in.web.response.RegisteredStockResponse;
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
public class InventoryStockInitializationHttpAdapter {

    private static final String DEFAULT_PATH = "/api/v1/stock-items";

    private final WebClient webClient;
    private final String initializePath;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public InventoryStockInitializationHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.inventory.base-url:http://inventory-service}") String baseUrl,
            @Value("${app.external.inventory.initialize-stock-path:}") String initializePath,
            @Value("${app.external.inventory.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.inventory.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.inventory.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.initializePath = initializePath == null || initializePath.isBlank() ? DEFAULT_PATH : initializePath;
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public Mono<RegisteredStockResponse> initializeStockItem(
            String warehouseId,
            String sku,
            Integer initialPhysicalQty,
            Integer reorderPoint,
            Integer safetyStock,
            String idempotencyKey) {
        return webClient
                .post()
                .uri(initializePath)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey == null ? "" : idempotencyKey.trim())
                .bodyValue(new InitializeStockItemPayload(
                        warehouseId,
                        sku,
                        initialPhysicalQty,
                        reorderPoint,
                        safetyStock))
                .retrieve()
                .bodyToMono(RegisteredStockResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof TimeoutException
                || throwable instanceof WebClientRequestException;
    }

    private record InitializeStockItemPayload(
            String warehouseId,
            String sku,
            Integer initialPhysicalQty,
            Integer reorderPoint,
            Integer safetyStock) {
    }
}
