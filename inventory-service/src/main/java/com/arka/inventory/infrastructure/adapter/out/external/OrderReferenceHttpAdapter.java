package com.arka.inventory.infrastructure.adapter.out.external;

import com.arka.inventory.application.port.out.external.OrderReferencePort;
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
public class OrderReferenceHttpAdapter implements OrderReferencePort {

    private static final String DEFAULT_CART_PATH = "/api/v1/internal/carts/{cartId}/organization-context";
    private static final String DEFAULT_ORDER_PATH = "/api/v1/internal/orders/{orderId}/organization-context";

    private final WebClient webClient;
    private final String cartPath;
    private final String orderPath;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public OrderReferenceHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.order.base-url:http://order-service}") String baseUrl,
            @Value("${app.external.order.cart-path:}") String cartPath,
            @Value("${app.external.order.order-path:}") String orderPath,
            @Value("${app.external.order.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.order.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.order.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.cartPath = cartPath == null || cartPath.isBlank() ? DEFAULT_CART_PATH : cartPath;
        this.orderPath = orderPath == null || orderPath.isBlank() ? DEFAULT_ORDER_PATH : orderPath;
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public OrderReferenceHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String cartPath,
            String orderPath,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, cartPath, orderPath, timeoutMs, 2, 200L);
    }

    @Override
    public Mono<Boolean> isValidCartReference(String organizationId, String cartId) {
        if (organizationId == null || organizationId.isBlank() || cartId == null || cartId.isBlank()) {
            return Mono.just(false);
        }
        return validateReference(cartPath, cartId.trim());
    }

    @Override
    public Mono<Boolean> isValidOrderReference(String organizationId, String orderId) {
        if (organizationId == null || organizationId.isBlank() || orderId == null || orderId.isBlank()) {
            return Mono.just(false);
        }
        return validateReference(orderPath, orderId.trim());
    }

    private Mono<Boolean> validateReference(String path, String resourceId) {
        return webClient
                .get()
                .uri(path, resourceId)
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
                                .flatMap(body -> Mono.error(clientError(status, resourceId, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Order reference validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
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
