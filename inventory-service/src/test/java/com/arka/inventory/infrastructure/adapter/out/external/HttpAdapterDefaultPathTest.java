package com.arka.inventory.infrastructure.adapter.out.external;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class HttpAdapterDefaultPathTest {

    @Test
    void shouldUseDirectoryDefaultOrganizationPathWhenPropertyIsBlank() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            capturedPath.set(request.url().getPath());
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        DirectoryOrganizationHttpAdapter adapter = new DirectoryOrganizationHttpAdapter(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://directory-service:8080",
                "",
                3_000);

        StepVerifier.create(adapter.organizationExists("org-123"))
                .expectNext(true)
                .verifyComplete();

        assertEquals("/api/v1/internal/organizations/org-123", capturedPath.get());
    }

    @Test
    void shouldUseOrderDefaultCartAndOrderPathsWhenPropertiesAreBlank() {
        AtomicReference<String> cartPath = new AtomicReference<>();
        AtomicReference<String> orderPath = new AtomicReference<>();

        ExchangeFunction exchangeFunction = request -> {
            String path = request.url().getPath();
            if (path.contains("/carts/")) {
                cartPath.set(path);
            }
            if (path.contains("/orders/")) {
                orderPath.set(path);
            }
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        OrderReferenceHttpAdapter adapter = new OrderReferenceHttpAdapter(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://order-service:8080",
                "",
                "",
                3_000);

        StepVerifier.create(adapter.isValidCartReference("organization-1", "cart-001"))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(adapter.isValidOrderReference("organization-1", "order-009"))
                .expectNext(true)
                .verifyComplete();

        assertEquals("/api/v1/internal/carts/cart-001/organization-context", cartPath.get());
        assertEquals("/api/v1/internal/orders/order-009/organization-context", orderPath.get());
    }
}
