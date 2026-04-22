package com.arka.reporting.infrastructure.adapter.out.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class ReportingExternalHttpAdapterStatusTest {

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
    void orderOrganizationLookupShouldTreat404AsAbsenceAndDifferentiate4xx5xx() {
        OrderOrganizationLookupHttpAdapter ok = orderAdapter(
                HttpStatus.OK,
                """
                {"organizationId":"organization-1","organizationId":"org-1"}
                """,
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(ok.resolveOrganizationByOrderId("order-1"))
                .expectNext("org-1")
                .verifyComplete();

        OrderOrganizationLookupHttpAdapter notFound = orderAdapter(
                HttpStatus.NOT_FOUND,
                "{\"error\":\"not-found\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(notFound.resolveOrganizationByOrderId("order-1")).verifyComplete();

        OrderOrganizationLookupHttpAdapter badRequest = orderAdapter(
                HttpStatus.BAD_REQUEST,
                "{\"error\":\"bad\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(badRequest.resolveOrganizationByOrderId("order-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        OrderOrganizationLookupHttpAdapter unauthorized = orderAdapter(
                HttpStatus.UNAUTHORIZED,
                "{\"error\":\"unauthorized\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(unauthorized.resolveOrganizationByOrderId("order-1"))
                .expectError(SecurityException.class)
                .verify();

        OrderOrganizationLookupHttpAdapter forbidden = orderAdapter(
                HttpStatus.FORBIDDEN,
                "{\"error\":\"forbidden\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(forbidden.resolveOrganizationByOrderId("order-1"))
                .expectError(SecurityException.class)
                .verify();

        OrderOrganizationLookupHttpAdapter conflict = orderAdapter(
                HttpStatus.CONFLICT,
                "{\"error\":\"conflict\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(conflict.resolveOrganizationByOrderId("order-1"))
                .expectError(IllegalStateException.class)
                .verify();

        OrderOrganizationLookupHttpAdapter unprocessable = orderAdapter(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "{\"error\":\"unprocessable\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(unprocessable.resolveOrganizationByOrderId("order-1"))
                .expectError(IllegalStateException.class)
                .verify();

        OrderOrganizationLookupHttpAdapter serverError = orderAdapter(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "{\"error\":\"boom\"}",
                "/api/v1/orders/{orderId}",
                "/api/v1/carts/{cartId}");
        StepVerifier.create(serverError.resolveOrganizationByOrderId("order-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void directoryRegionalPolicyShouldTreat404AsUnavailableAndDifferentiate4xx5xx() {
        DirectoryRegionalPolicyHttpAdapter ok = regionalAdapter(
                HttpStatus.OK,
                """
                {"countryPolicy":{"policyId":"policy-1","status":"ACTIVE"}}
                """,
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(ok.resolveForOperation("organization-1", "CO"))
                .assertNext(resolution -> {
                    assertEquals("organization-1", resolution.organizationId());
                    assertTrue(resolution.available());
                    assertEquals("policy-1", resolution.policyRef());
                })
                .verifyComplete();

        DirectoryRegionalPolicyHttpAdapter notFound = regionalAdapter(
                HttpStatus.NOT_FOUND,
                "{\"error\":\"not-found\"}",
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(notFound.resolveForOperation("organization-1", "CO"))
                .assertNext(resolution -> assertFalse(resolution.available()))
                .verifyComplete();

        DirectoryRegionalPolicyHttpAdapter badRequest = regionalAdapter(
                HttpStatus.BAD_REQUEST,
                "{\"error\":\"bad\"}",
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(badRequest.resolveForOperation("organization-1", "CO"))
                .expectError(IllegalArgumentException.class)
                .verify();

        DirectoryRegionalPolicyHttpAdapter unauthorized = regionalAdapter(
                HttpStatus.UNAUTHORIZED,
                "{\"error\":\"unauthorized\"}",
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(unauthorized.resolveForOperation("organization-1", "CO"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryRegionalPolicyHttpAdapter forbidden = regionalAdapter(
                HttpStatus.FORBIDDEN,
                "{\"error\":\"forbidden\"}",
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(forbidden.resolveForOperation("organization-1", "CO"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryRegionalPolicyHttpAdapter conflict = regionalAdapter(
                HttpStatus.CONFLICT,
                "{\"error\":\"conflict\"}",
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(conflict.resolveForOperation("organization-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryRegionalPolicyHttpAdapter unprocessable = regionalAdapter(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "{\"error\":\"unprocessable\"}",
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(unprocessable.resolveForOperation("organization-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryRegionalPolicyHttpAdapter serverError = regionalAdapter(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "{\"error\":\"boom\"}",
                "/api/v1/organizations/{organizationId}/regional-context/{countryCode}");
        StepVerifier.create(serverError.resolveForOperation("organization-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldUseDefaultPathsWhenConfiguredPathsAreBlank() {
        AtomicReference<String> orderPath = new AtomicReference<>();
        AtomicReference<String> cartPath = new AtomicReference<>();
        AtomicReference<String> regionalPath = new AtomicReference<>();

        ExchangeFunction exchangeFunction = request -> {
            String path = request.url().getPath();
            if (path.contains("/orders/")) {
                orderPath.set(path);
            }
            if (path.contains("/carts/")) {
                cartPath.set(path);
            }
            if (path.contains("/regional-context/")) {
                regionalPath.set(path);
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"error\":\"not-found\"}")
                    .build());
        };

        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        OrderOrganizationLookupHttpAdapter orderAdapter = new OrderOrganizationLookupHttpAdapter(
                builder,
                "http://order-service",
                "",
                "",
                3_000);
        DirectoryRegionalPolicyHttpAdapter regionalAdapter = new DirectoryRegionalPolicyHttpAdapter(
                builder,
                "http://directory-service",
                "",
                3_000);

        StepVerifier.create(orderAdapter.resolveOrganizationByOrderId("order-9")).verifyComplete();
        StepVerifier.create(orderAdapter.resolveOrganizationByCartId("cart-7")).verifyComplete();
        StepVerifier.create(regionalAdapter.resolveForOperation("organization-1", "CO"))
                .assertNext(resolution -> assertFalse(resolution.available()))
                .verifyComplete();

        assertEquals("/api/v1/internal/orders/order-9/organization-context", orderPath.get());
        assertEquals("/api/v1/internal/carts/cart-7/organization-context", cartPath.get());
        assertEquals("/api/v1/internal/organizations/organization-1/regional-context/CO", regionalPath.get());
    }

    private IdentityActorLegitimacyHttpAdapter legitimacyAdapter(HttpStatus status, String body) {
        return new IdentityActorLegitimacyHttpAdapter(
                builder(status, body),
                "http://identity-access-service",
                "/api/v1/admin/iam/users/{actorId}/permissions",
                3_000);
    }

    private OrderOrganizationLookupHttpAdapter orderAdapter(HttpStatus status, String body, String orderPath, String cartPath) {
        return new OrderOrganizationLookupHttpAdapter(
                builder(status, body),
                "http://order-service",
                orderPath,
                cartPath,
                3_000);
    }

    private DirectoryRegionalPolicyHttpAdapter regionalAdapter(HttpStatus status, String body, String path) {
        return new DirectoryRegionalPolicyHttpAdapter(
                builder(status, body),
                "http://directory-service",
                path,
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
