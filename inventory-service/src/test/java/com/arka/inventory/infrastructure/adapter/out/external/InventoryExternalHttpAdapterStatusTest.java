package com.arka.inventory.infrastructure.adapter.out.external;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class InventoryExternalHttpAdapterStatusTest {

    @Test
    void catalogSkuShouldTreat404AsNotFoundAndDifferentiate4xx5xx() {
        CatalogSkuHttpAdapter notFound = catalogAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.existsSellableSku("tenant-1", "SKU-1"))
                .expectNext(false)
                .verifyComplete();

        CatalogSkuHttpAdapter badRequest = catalogAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.existsSellableSku("tenant-1", "SKU-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        CatalogSkuHttpAdapter unauthorized = catalogAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.existsSellableSku("tenant-1", "SKU-1"))
                .expectError(SecurityException.class)
                .verify();

        CatalogSkuHttpAdapter forbidden = catalogAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.existsSellableSku("tenant-1", "SKU-1"))
                .expectError(SecurityException.class)
                .verify();

        CatalogSkuHttpAdapter conflict = catalogAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.existsSellableSku("tenant-1", "SKU-1"))
                .expectError(IllegalStateException.class)
                .verify();

        CatalogSkuHttpAdapter unprocessable =
                catalogAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.existsSellableSku("tenant-1", "SKU-1"))
                .expectError(IllegalStateException.class)
                .verify();

        CatalogSkuHttpAdapter serverError = catalogAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.existsSellableSku("tenant-1", "SKU-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void directoryTenantShouldTreat404AsNotFoundAndDifferentiate4xx5xx() {
        DirectoryTenantHttpAdapter notFound = directoryAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.tenantExists("tenant-1"))
                .expectNext(false)
                .verifyComplete();

        DirectoryTenantHttpAdapter badRequest = directoryAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.tenantExists("tenant-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        DirectoryTenantHttpAdapter unauthorized = directoryAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.tenantExists("tenant-1"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryTenantHttpAdapter forbidden = directoryAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.tenantExists("tenant-1"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryTenantHttpAdapter conflict = directoryAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.tenantExists("tenant-1"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryTenantHttpAdapter unprocessable =
                directoryAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.tenantExists("tenant-1"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryTenantHttpAdapter serverError = directoryAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.tenantExists("tenant-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void identityLegitimacyShouldReturnFalseOn404AndErrorOnOther4xx5xx() {
        IdentityActorLegitimacyHttpAdapter notFound = legitimacyAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.isLegitimate("actor-1"))
                .expectNext(false)
                .verifyComplete();

        IdentityActorLegitimacyHttpAdapter badRequest = legitimacyAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.isLegitimate("actor-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unauthorized =
                legitimacyAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.isLegitimate("actor-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter forbidden = legitimacyAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.isLegitimate("actor-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter conflict = legitimacyAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.isLegitimate("actor-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unprocessable =
                legitimacyAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.isLegitimate("actor-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter serverError =
                legitimacyAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.isLegitimate("actor-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void orderReferenceShouldTreat404AsNotFoundAndDifferentiate4xx5xx() {
        OrderReferenceHttpAdapter notFound = orderAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.isValidOrderReference("tenant-1", "order-1"))
                .expectNext(false)
                .verifyComplete();

        OrderReferenceHttpAdapter badRequest = orderAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.isValidOrderReference("tenant-1", "order-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        OrderReferenceHttpAdapter unauthorized = orderAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.isValidOrderReference("tenant-1", "order-1"))
                .expectError(SecurityException.class)
                .verify();

        OrderReferenceHttpAdapter forbidden = orderAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.isValidOrderReference("tenant-1", "order-1"))
                .expectError(SecurityException.class)
                .verify();

        OrderReferenceHttpAdapter conflict = orderAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.isValidOrderReference("tenant-1", "order-1"))
                .expectError(IllegalStateException.class)
                .verify();

        OrderReferenceHttpAdapter unprocessable =
                orderAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.isValidOrderReference("tenant-1", "order-1"))
                .expectError(IllegalStateException.class)
                .verify();

        OrderReferenceHttpAdapter serverError = orderAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.isValidOrderReference("tenant-1", "order-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    private CatalogSkuHttpAdapter catalogAdapter(HttpStatus status, String body) {
        return new CatalogSkuHttpAdapter(
                builder(status, body),
                "http://catalog-service",
                "/api/v1/catalog/checkout/variant-resolution",
                "",
                "COP",
                "BASE",
                3_000);
    }

    private DirectoryTenantHttpAdapter directoryAdapter(HttpStatus status, String body) {
        return new DirectoryTenantHttpAdapter(
                builder(status, body),
                "http://directory-service",
                "/api/v1/organizations/{organizationId}",
                "",
                3_000);
    }

    private IdentityActorLegitimacyHttpAdapter legitimacyAdapter(HttpStatus status, String body) {
        return new IdentityActorLegitimacyHttpAdapter(
                builder(status, body),
                "http://identity-access-service",
                "/api/v1/admin/iam/users/{actorId}/permissions",
                "",
                3_000);
    }

    private OrderReferenceHttpAdapter orderAdapter(HttpStatus status, String body) {
        return new OrderReferenceHttpAdapter(
                builder(status, body),
                "http://order-service",
                "/api/v1/carts/{cartId}",
                "/api/v1/orders/{orderId}",
                "",
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
