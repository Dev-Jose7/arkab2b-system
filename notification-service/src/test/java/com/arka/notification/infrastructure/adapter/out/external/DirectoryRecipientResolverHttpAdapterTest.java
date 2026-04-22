package com.arka.notification.infrastructure.adapter.out.external;

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

class DirectoryRecipientResolverHttpAdapterTest {

    @Test
    void shouldReturnRecipientWhenStatusIs200() {
        DirectoryRecipientResolverHttpAdapter adapter = adapterFor(
                HttpStatus.OK,
                """
                [
                  {"status":"ACTIVE","contactType":"EMAIL","value":"buyer@arka.com"}
                ]
                """);

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL"))
                .assertNext(result -> {
                    assertEquals("org-1", result.recipientRef());
                    assertEquals("EMAIL", result.channel());
                    assertEquals("buyer@arka.com", result.destination());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenStatusIs404() {
        DirectoryRecipientResolverHttpAdapter adapter = adapterFor(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL")).verifyComplete();
    }

    @Test
    void shouldFailWhenStatusIs400() {
        DirectoryRecipientResolverHttpAdapter adapter = adapterFor(HttpStatus.BAD_REQUEST, "{\"error\":\"bad-request\"}");

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs401() {
        DirectoryRecipientResolverHttpAdapter adapter = adapterFor(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL"))
                .expectError(SecurityException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs403() {
        DirectoryRecipientResolverHttpAdapter adapter = adapterFor(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL"))
                .expectError(SecurityException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs409() {
        DirectoryRecipientResolverHttpAdapter adapter = adapterFor(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs422() {
        DirectoryRecipientResolverHttpAdapter adapter =
                adapterFor(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldFailWhenStatusIs5xx() {
        DirectoryRecipientResolverHttpAdapter adapter =
                adapterFor(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"provider-down\"}");

        StepVerifier.create(adapter.resolve("organization-1", "org-1", "EMAIL"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    private DirectoryRecipientResolverHttpAdapter adapterFor(HttpStatus status, String body) {
        ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        return new DirectoryRecipientResolverHttpAdapter(
                builder,
                "http://directory-service",
                "/api/v1/organizations/{organizationId}/contacts",
                3_000);
    }
}
