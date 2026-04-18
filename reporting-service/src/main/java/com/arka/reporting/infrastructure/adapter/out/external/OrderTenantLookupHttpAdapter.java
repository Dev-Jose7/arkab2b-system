package com.arka.reporting.infrastructure.adapter.out.external;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OrderTenantLookupHttpAdapter {

    private static final String DEFAULT_ORDER_PATH = "/api/v1/orders/{orderId}";
    private static final String DEFAULT_CART_PATH = "/api/v1/carts/{cartId}";

    private final WebClient webClient;
    private final String orderPath;
    private final String cartPath;
    private final String serviceToken;
    private final Duration timeout;

    public OrderTenantLookupHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.order.base-url:http://order-service:8080}") String baseUrl,
            @Value("${app.external.order.order-path:}") String orderPath,
            @Value("${app.external.order.cart-path:}") String cartPath,
            @Value("${app.external.order.service-token:}") String serviceToken,
            @Value("${app.external.order.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.orderPath = orderPath == null || orderPath.isBlank() ? DEFAULT_ORDER_PATH : orderPath;
        this.cartPath = cartPath == null || cartPath.isBlank() ? DEFAULT_CART_PATH : cartPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    public Mono<String> resolveTenantByOrderId(String orderId) {
        return resolve(orderPath, orderId, "order");
    }

    public Mono<String> resolveTenantByCartId(String cartId) {
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
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .flatMap(this::extractTenant);
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
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Order tenant lookup failed status="
                                            + status
                                            + " resourceType="
                                            + resourceType
                                            + " resourceId="
                                            + resourceId
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String resourceType, String resourceId, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Order tenant lookup request rejected (400). resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            case 401, 403 -> new SecurityException(
                    "Order tenant lookup unauthorized/forbidden. status="
                            + status
                            + " resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            case 409 -> new IllegalStateException(
                    "Order tenant lookup conflict (409). resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            case 422 -> new IllegalStateException(
                    "Order tenant lookup semantic error (422). resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
            default -> new IllegalStateException(
                    "Order tenant lookup client error. status="
                            + status
                            + " resourceType="
                            + resourceType
                            + " resourceId="
                            + resourceId
                            + " body="
                            + body);
        };
    }

    private Mono<String> extractTenant(JsonNode root) {
        if (root == null || root.isNull()) {
            return Mono.empty();
        }
        JsonNode payload = extractPayloadNode(root);
        String tenantId = text(payload, "tenantId");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = text(payload, "organizationId");
        }
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.empty();
        }
        return Mono.just(tenantId);
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

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }
}
