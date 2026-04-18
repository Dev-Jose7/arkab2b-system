package com.arka.order.infrastructure.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class OrderHttpConsumerContractTest {

    @Test
    void catalogVariantAdapterShouldRespectConsumerContract() throws Exception {
        try (StubHttpServer server = StubHttpServer.responding(
                200,
                """
                {
                  "variantId":"var-1",
                  "sku":"SKU-1",
                  "amount":"12500.00",
                  "currency":"COP"
                }
                """)) {
            CatalogVariantHttpAdapter adapter = new CatalogVariantHttpAdapter(
                    WebClient.builder(),
                    server.baseUrl(),
                    "/api/v1/catalog/checkout/variant-resolution",
                    "order-service-token",
                    "COP",
                    "BASE",
                    3_000);

            StepVerifier.create(adapter.resolveVariant("organization-1", "ignored", "SKU-1"))
                    .assertNext(snapshot -> {
                        assertThat(snapshot.variantId()).isEqualTo("var-1");
                        assertThat(snapshot.sku()).isEqualTo("SKU-1");
                        assertThat(snapshot.currency()).isEqualTo("COP");
                        assertThat(snapshot.sellable()).isTrue();
                    })
                    .verifyComplete();

            CapturedRequest request = server.lastRequest();
            assertThat(request.method()).isEqualTo("GET");
            assertThat(request.path()).isEqualTo("/api/v1/catalog/checkout/variant-resolution");
            assertThat(request.query()).contains("sku=SKU-1");
            assertThat(request.query()).contains("currency=COP");
            assertThat(request.query()).contains("priceType=BASE");
            assertThat(request.header("Authorization")).isEqualTo("Bearer order-service-token");
        }
    }

    @Test
    void directoryCheckoutAdapterShouldRespectConsumerContract() throws Exception {
        try (StubHttpServer server = StubHttpServer.responding(
                200,
                """
                {
                  "resolutionStatus":"RESOLVED",
                  "address":{"countryCode":"CO","validationStatus":"VALID"},
                  "countryPolicy":{"policyVersion":17,"currencyCode":"COP","status":"ACTIVE"}
                }
                """)) {
            DirectoryCheckoutHttpAdapter adapter = new DirectoryCheckoutHttpAdapter(
                    WebClient.builder(),
                    server.baseUrl(),
                    "/api/v1/organizations/{organizationId}/addresses/{addressId}/checkout-resolution",
                    "order-service-token",
                    3_000);

            StepVerifier.create(adapter.resolveCheckoutContext("organization-1", "addr-22", "co"))
                    .assertNext(context -> {
                        assertThat(context.organizationId()).isEqualTo("organization-1");
                        assertThat(context.addressId()).isEqualTo("addr-22");
                        assertThat(context.countryCode()).isEqualTo("CO");
                        assertThat(context.policyActive()).isTrue();
                        assertThat(context.addressValid()).isTrue();
                    })
                    .verifyComplete();

            CapturedRequest request = server.lastRequest();
            assertThat(request.method()).isEqualTo("GET");
            assertThat(request.path()).isEqualTo("/api/v1/organizations/organization-1/addresses/addr-22/checkout-resolution");
            assertThat(request.query()).isEqualTo("countryCode=CO");
            assertThat(request.header("Authorization")).isEqualTo("Bearer order-service-token");
        }
    }

    @Test
    void inventoryReservationAdapterShouldRespectConsumerContract() throws Exception {
        try (StubHttpServer server = StubHttpServer.responding(
                200,
                """
                {
                  "reservationId":"res-77",
                  "sku":"SKU-9",
                  "qty":3,
                  "reservationConfirmed":true,
                  "commitableAvailable":true
                }
                """)) {
            InventoryReservationHttpAdapter adapter = new InventoryReservationHttpAdapter(
                    WebClient.builder(),
                    server.baseUrl(),
                    "/api/v1/internal/reservations/{reservationId}/validation",
                    "order-service-token",
                    3_000);

            StepVerifier.create(adapter.validateReservation("organization-9", "res-77", "sku-9", 3))
                    .assertNext(validation -> {
                        assertThat(validation.reservationId()).isEqualTo("res-77");
                        assertThat(validation.sku()).isEqualTo("SKU-9");
                        assertThat(validation.qty()).isEqualTo(3);
                        assertThat(validation.reservationConfirmed()).isTrue();
                        assertThat(validation.commitableAvailable()).isTrue();
                    })
                    .verifyComplete();

            CapturedRequest request = server.lastRequest();
            assertThat(request.method()).isEqualTo("GET");
            assertThat(request.path()).isEqualTo("/api/v1/internal/reservations/res-77/validation");
            assertThat(request.query()).contains("organizationId=organization-9");
            assertThat(request.query()).contains("sku=SKU-9");
            assertThat(request.query()).contains("qty=3");
            assertThat(request.header("Authorization")).isEqualTo("Bearer order-service-token");
        }
    }

    private static final class StubHttpServer implements AutoCloseable {

        private final HttpServer server;
        private final AtomicReference<CapturedRequest> lastRequest = new AtomicReference<>();
        private final int status;
        private final String body;

        private StubHttpServer(int status, String body) throws IOException {
            this.status = status;
            this.body = body == null ? "" : body;
            this.server = HttpServer.create(new InetSocketAddress(0), 0);
            this.server.createContext("/", this::handle);
            this.server.start();
        }

        static StubHttpServer responding(int status, String body) throws IOException {
            return new StubHttpServer(status, body);
        }

        String baseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        CapturedRequest lastRequest() {
            return Objects.requireNonNull(lastRequest.get(), "No request captured");
        }

        private void handle(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, List<String>> headerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            exchange.getRequestHeaders().forEach((key, value) -> headerMap.put(key, List.copyOf(value)));
            lastRequest.set(new CapturedRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getQuery(),
                    headerMap,
                    requestBody));

            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, payload.length);
            exchange.getResponseBody().write(payload);
            exchange.close();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }

    private record CapturedRequest(
            String method,
            String path,
            String query,
            Map<String, List<String>> headers,
            String body) {

        String header(String name) {
            List<String> values = headers.get(name);
            return values == null || values.isEmpty() ? null : values.getFirst();
        }
    }
}
