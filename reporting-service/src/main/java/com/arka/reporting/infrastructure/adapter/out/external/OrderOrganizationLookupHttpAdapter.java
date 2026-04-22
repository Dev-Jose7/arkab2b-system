package com.arka.reporting.infrastructure.adapter.out.external;

import com.fasterxml.jackson.databind.JsonNode;
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
public class OrderOrganizationLookupHttpAdapter {

    private static final String DEFAULT_ORDER_PATH = "/api/v1/internal/orders/{orderId}/organization-context";
    private static final String DEFAULT_CART_PATH = "/api/v1/internal/carts/{cartId}/organization-context";

    private final WebClient webClient;
    private final String orderPath;
    private final String cartPath;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public OrderOrganizationLookupHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.order.base-url:http://order-service}") String baseUrl,
            @Value("${app.external.order.order-path:}") String orderPath,
            @Value("${app.external.order.cart-path:}") String cartPath,
            @Value("${app.external.order.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.order.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.order.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.orderPath = orderPath == null || orderPath.isBlank() ? DEFAULT_ORDER_PATH : orderPath;
        this.cartPath = cartPath == null || cartPath.isBlank() ? DEFAULT_CART_PATH : cartPath;
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public OrderOrganizationLookupHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String orderPath,
            String cartPath,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, orderPath, cartPath, timeoutMs, 2, 200L);
    }

    public Mono<String> resolveOrganizationByOrderId(String orderId) {
        return resolve(orderPath, orderId, "order");
    }

    public Mono<String> resolveOrganizationByCartId(String cartId) {
        return resolve(cartPath, cartId, "cart");
    }

    private Mono<String> resolve(String path, String resourceId, String resourceType) {
        if (resourceId == null || resourceId.isBlank()) {
            return Mono.empty();
        }
        return webClient
                .get()
                .uri(path, resourceId.trim())
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .flatMap(this::extractOrganizationId);
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.empty();
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, resourceType, resourceId, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Order organization-context lookup failed status="
                                            + status
                                            + " resourceType="
                                            + resourceType
                                            + " resourceId="
                                            + resourceId
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private RuntimeException clientError(int status, String resourceType, String resourceId, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Order organization-context lookup request rejected (400). resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            case 401, 403 -> new SecurityException(
                    "Order organization-context lookup unauthorized/forbidden. status="
                            + status
                            + " resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            case 409 -> new IllegalStateException(
                    "Order organization-context lookup conflict (409). resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            case 422 -> new IllegalStateException(
                    "Order organization-context lookup semantic error (422). resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            default -> new IllegalStateException(
                    "Order organization-context lookup client error. status="
                            + status
                            + " resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
        };
    }

    private Mono<String> extractOrganizationId(JsonNode root) {
        if (root == null || root.isNull()) {
            return Mono.empty();
        }
        JsonNode payload = extractPayloadNode(root);
        String organizationId = text(payload, "organizationId");
        if (organizationId == null || organizationId.isBlank()) {
            organizationId = text(payload, "organizationId");
        }
        if (organizationId == null || organizationId.isBlank()) {
            organizationId = text(payload, "organizationId");
        }
        if (organizationId == null || organizationId.isBlank()) {
            return Mono.empty();
        }
        return Mono.just(organizationId);
    }

    private JsonNode extractPayloadNode(JsonNode root) {
        JsonNode dataNode = root.get("data");
        if (dataNode != null && dataNode.isObject()) {
            return dataNode;
        }
        return root;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String parsed = value.asText("").trim();
        return parsed.isBlank() ? null : parsed;
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
