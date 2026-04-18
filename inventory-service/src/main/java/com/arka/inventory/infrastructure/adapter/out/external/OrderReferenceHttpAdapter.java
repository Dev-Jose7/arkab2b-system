package com.arka.inventory.infrastructure.adapter.out.external;

import com.arka.inventory.application.port.out.external.OrderReferencePort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OrderReferenceHttpAdapter implements OrderReferencePort {

    private static final String DEFAULT_CART_PATH = "/api/v1/carts/{cartId}";
    private static final String DEFAULT_ORDER_PATH = "/api/v1/orders/{orderId}";

    private final WebClient webClient;
    private final String cartPath;
    private final String orderPath;
    private final String serviceToken;
    private final Duration timeout;

    public OrderReferenceHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.order.base-url:http://order-service:8080}") String baseUrl,
            @Value("${app.external.order.cart-path:}") String cartPath,
            @Value("${app.external.order.order-path:}") String orderPath,
            @Value("${app.external.order.service-token:}") String serviceToken,
            @Value("${app.external.order.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.cartPath = cartPath == null || cartPath.isBlank() ? DEFAULT_CART_PATH : cartPath;
        this.orderPath = orderPath == null || orderPath.isBlank() ? DEFAULT_ORDER_PATH : orderPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<Boolean> isValidCartReference(String tenantId, String cartId) {
        if (tenantId == null || tenantId.isBlank() || cartId == null || cartId.isBlank()) {
            return Mono.just(false);
        }
        return validateReference(cartPath, cartId.trim());
    }

    @Override
    public Mono<Boolean> isValidOrderReference(String tenantId, String orderId) {
        if (tenantId == null || tenantId.isBlank() || orderId == null || orderId.isBlank()) {
            return Mono.just(false);
        }
        return validateReference(orderPath, orderId.trim());
    }

    private Mono<Boolean> validateReference(String path, String resourceId) {
        return webClient
                .get()
                .uri(path, resourceId)
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
                                .flatMap(body -> Mono.error(clientError(status, resourceId, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Order reference validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String resourceId, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Order reference validation request rejected (400). resourceId=" + resourceId + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Order reference validation unauthorized/forbidden. status=" + status + " resourceId="
                            + resourceId + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Order reference validation conflict (409). resourceId=" + resourceId + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Order reference validation semantic error (422). resourceId=" + resourceId + " body=" + body);
            default -> new IllegalStateException(
                    "Order reference validation client error. status=" + status + " resourceId=" + resourceId
                            + " body=" + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }
}
