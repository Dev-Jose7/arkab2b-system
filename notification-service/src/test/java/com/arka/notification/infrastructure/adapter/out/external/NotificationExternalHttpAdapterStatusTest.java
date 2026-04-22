package com.arka.notification.infrastructure.adapter.out.external;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class NotificationExternalHttpAdapterStatusTest {

    @Test
    void identityLegitimacyShouldReturnFalseOn404AndErrorOnOther4xx5xx() {
        IdentityActorLegitimacyHttpAdapter notFound = legitimacyAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.isLegitimate("actor-1", "organization-1"))
                .expectNext(false)
                .verifyComplete();

        IdentityActorLegitimacyHttpAdapter badRequest = legitimacyAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unauthorized =
                legitimacyAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.isLegitimate("actor-1", "organization-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter forbidden = legitimacyAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.isLegitimate("actor-1", "organization-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter conflict = legitimacyAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unprocessable =
                legitimacyAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter serverError =
                legitimacyAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void orderContextShouldTreat404AsAbsenceAndDifferentiate4xx5xx() {
        OrderContextLookupHttpAdapter ok = orderAdapter(
                HttpStatus.OK,
                """
                {"organizationId":"organization-1","userId":"user-1"}
                """,
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(ok.resolveByOrderId("order-1"))
                .assertNext(context -> {
                    assertEquals("organization-1", context.organizationId());
                    assertEquals("user-1", context.actorId());
                })
                .verifyComplete();

        OrderContextLookupHttpAdapter notFound = orderAdapter(
                HttpStatus.NOT_FOUND,
                "{\"error\":\"not-found\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(notFound.resolveByOrderId("order-1")).verifyComplete();

        OrderContextLookupHttpAdapter badRequest = orderAdapter(
                HttpStatus.BAD_REQUEST,
                "{\"error\":\"bad\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(badRequest.resolveByOrderId("order-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        OrderContextLookupHttpAdapter unauthorized = orderAdapter(
                HttpStatus.UNAUTHORIZED,
                "{\"error\":\"unauthorized\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(unauthorized.resolveByOrderId("order-1"))
                .expectError(SecurityException.class)
                .verify();

        OrderContextLookupHttpAdapter forbidden = orderAdapter(
                HttpStatus.FORBIDDEN,
                "{\"error\":\"forbidden\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(forbidden.resolveByOrderId("order-1"))
                .expectError(SecurityException.class)
                .verify();

        OrderContextLookupHttpAdapter conflict = orderAdapter(
                HttpStatus.CONFLICT,
                "{\"error\":\"conflict\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(conflict.resolveByOrderId("order-1"))
                .expectError(IllegalStateException.class)
                .verify();

        OrderContextLookupHttpAdapter unprocessable = orderAdapter(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "{\"error\":\"unprocessable\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(unprocessable.resolveByOrderId("order-1"))
                .expectError(IllegalStateException.class)
                .verify();

        OrderContextLookupHttpAdapter serverError = orderAdapter(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "{\"error\":\"boom\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(serverError.resolveByOrderId("order-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldUseDefaultOrderAndCartPathsWhenConfiguredPathsAreBlank() {
        AtomicReference<String> orderPath = new AtomicReference<>();
        AtomicReference<String> cartPath = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            String path = request.url().getPath();
            if (path.contains("/orders/")) {
                orderPath.set(path);
            }
            if (path.contains("/carts/")) {
                cartPath.set(path);
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"error\":\"not-found\"}")
                    .build());
        };
        OrderContextLookupHttpAdapter adapter = new OrderContextLookupHttpAdapter(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://order-service",
                "",
                "",
                3_000);

        StepVerifier.create(adapter.resolveByOrderId("order-9")).verifyComplete();
        StepVerifier.create(adapter.resolveByCartId("cart-7")).verifyComplete();

        assertEquals("/api/v1/internal/orders/order-9/organization-context", orderPath.get());
        assertEquals("/api/v1/internal/carts/cart-7/organization-context", cartPath.get());
    }

    private IdentityActorLegitimacyHttpAdapter legitimacyAdapter(HttpStatus status, String body) {
        return new IdentityActorLegitimacyHttpAdapter(
                builder(status, body),
                "http://identity-access-service",
                "/api/v1/admin/iam/users/{actorId}/permissions",
                3_000);
    }

    private OrderContextLookupHttpAdapter orderAdapter(HttpStatus status, String body, String orderPath, String cartPath) {
        return new OrderContextLookupHttpAdapter(
                builder(status, body),
                "http://order-service",
                orderPath,
                cartPath,
                3_000);
    }

    private WebClient.Builder builder(HttpStatus status, String body) {
        ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
        return WebClient.builder().exchangeFunction(exchangeFunction);
    }
}
