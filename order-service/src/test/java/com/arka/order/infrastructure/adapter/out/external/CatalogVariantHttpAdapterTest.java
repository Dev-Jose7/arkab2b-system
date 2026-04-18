package com.arka.order.infrastructure.adapter.out.external;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CatalogVariantHttpAdapterTest {

    @Test
    void shouldReturnVariantWhenStatusIs200() {
        CatalogVariantHttpAdapter adapter = adapterFor(
                HttpStatus.OK,
                "{\"variantId\":\"v-1\",\"sku\":\"SKU-1\",\"amount\":\"100.00\",\"currency\":\"COP\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1"))
                .assertNext(result -> {
                    assertEquals("v-1", result.variantId());
                    assertEquals("SKU-1", result.sku());
                    assertEquals("COP", result.currency());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenStatusIs404() {
        CatalogVariantHttpAdapter adapter = adapterFor(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1")).verifyComplete();
    }

    @Test
    void shouldFailWhenStatusIs400() {
        CatalogVariantHttpAdapter adapter = adapterFor(HttpStatus.BAD_REQUEST, "{\"error\":\"bad-request\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs401() {
        CatalogVariantHttpAdapter adapter = adapterFor(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1"))
                .expectError(SecurityException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs403() {
        CatalogVariantHttpAdapter adapter = adapterFor(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1"))
                .expectError(SecurityException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs409() {
        CatalogVariantHttpAdapter adapter = adapterFor(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs422() {
        CatalogVariantHttpAdapter adapter = adapterFor(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs5xx() {
        CatalogVariantHttpAdapter adapter = adapterFor(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");

        StepVerifier.create(adapter.resolveVariant("organization-1", null, "SKU-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    private CatalogVariantHttpAdapter adapterFor(HttpStatus status, String body) {
        ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        return new CatalogVariantHttpAdapter(
                builder,
                "http://catalog-service",
                "/api/v1/catalog/checkout/variant-resolution",
                "",
                "COP",
                "BASE",
                3_000);
    }
}
