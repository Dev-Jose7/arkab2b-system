package com.arka.notification.infrastructure.adapter.out.external;

import com.arka.notification.application.port.out.external.NotificationProviderPort;
import com.arka.notification.application.port.out.external.ProviderSendRequest;
import com.arka.notification.application.port.out.external.ProviderSendResult;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationProviderHttpAdapter implements NotificationProviderPort {

    private final WebClient webClient;
    private final String sendPath;
    private final String authToken;
    private final Duration timeout;

    public NotificationProviderHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.provider.base-url:http://notification-provider:8088}") String baseUrl,
            @Value("${app.external.provider.send-path:/api/v1/messages/send}") String sendPath,
            @Value("${app.external.provider.auth-token:}") String authToken,
            @Value("${app.external.provider.timeout-ms:5000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.sendPath = sendPath;
        this.authToken = authToken == null ? "" : authToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<ProviderSendResult> send(ProviderSendRequest request) {
        Instant startedAt = Instant.now();
        return webClient
                .post()
                .uri(sendPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .bodyValue(new ProviderRequest(
                        request.tenantId(),
                        request.providerCode(),
                        request.channel(),
                        request.destination(),
                        request.renderedPayload(),
                        request.traceId(),
                        request.correlationId()))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .defaultIfEmpty(null)
                                .map(body -> toSuccess(startedAt, body));
                    }
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> toFailure(startedAt, response.statusCode().value(), body));
                })
                .timeout(timeout);
    }

    private ProviderSendResult toSuccess(Instant startedAt, JsonNode body) {
        String providerRef = text(body, "providerRef");
        boolean retryable = body != null && body.path("retryable").asBoolean(false);
        long latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
        String rawResponse = body == null ? "{}" : body.toString();
        return new ProviderSendResult(true, providerRef, null, null, latencyMs, retryable, rawResponse);
    }

    private ProviderSendResult toFailure(Instant startedAt, int statusCode, String body) {
        long latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
        boolean retryable = statusCode >= 500 || statusCode == 429;
        return new ProviderSendResult(
                false,
                null,
                "HTTP_" + statusCode,
                body == null || body.isBlank() ? "provider call failed" : body,
                latencyMs,
                retryable,
                body == null ? "" : body);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!authToken.isBlank()) {
            headers.setBearerAuth(authToken);
        }
    }

    private record ProviderRequest(
            String tenantId,
            String providerCode,
            String channel,
            String destination,
            String renderedPayload,
            String traceId,
            String correlationId) {}
}
