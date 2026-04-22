package com.arka.order.infrastructure.adapter.out.external;

import java.time.Duration;
import java.util.Map;
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
public class NotificationEmitterHttpAdapter {

    private static final String DEFAULT_EMIT_PATH = "/api/v1/notifications";

    private final WebClient webClient;
    private final String emitPath;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public NotificationEmitterHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.notification.base-url:http://notification-service}") String baseUrl,
            @Value("${app.external.notification.emit-path:}") String emitPath,
            @Value("${app.external.notification.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.notification.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.notification.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.emitPath = emitPath == null || emitPath.isBlank() ? DEFAULT_EMIT_PATH : emitPath;
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public Mono<Void> emit(
            String sourceEventId,
            String sourceEventType,
            String recipientRef,
            String channel,
            String payloadJson,
            String traceId,
            String correlationId,
            String idempotencyKey) {
        return webClient
                .post()
                .uri(emitPath)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "sourceEventId", sourceEventId,
                        "sourceEventType", sourceEventType,
                        "recipientRef", recipientRef,
                        "channel", channel,
                        "payloadJson", payloadJson == null ? "{}" : payloadJson,
                        "traceId", traceId == null ? "" : traceId,
                        "correlationId", correlationId == null ? "" : correlationId,
                        "idempotencyKey", idempotencyKey))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof TimeoutException
                || throwable instanceof WebClientRequestException;
    }
}
