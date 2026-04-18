package com.arka.order.infrastructure.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProtectedPurchaseLightE2eTest {

    @Test
    void shouldResolveCheckoutVariantAndReservationAcrossCoreServices() throws Exception {
        try (StubHttpServer directoryServer = StubHttpServer.responding(
                        200,
                        """
                        {
                          "resolutionStatus":"RESOLVED",
                          "address":{"countryCode":"CO","validationStatus":"VALID"},
                          "countryPolicy":{"policyVersion":4,"currencyCode":"COP","status":"ACTIVE"}
                        }
                        """);
                StubHttpServer catalogServer = StubHttpServer.responding(
                        200,
                        """
                        {
                          "variantId":"var-900",
                          "sku":"SKU-900",
                          "amount":"89990.00",
                          "currency":"COP"
                        }
                        """);
                StubHttpServer inventoryServer = StubHttpServer.responding(
                        200,
                        """
                        {
                          "reservationId":"res-900",
                          "sku":"SKU-900",
                          "qty":1,
                          "reservationConfirmed":true,
                          "commitableAvailable":true
                        }
                        """)) {

            DirectoryCheckoutHttpAdapter directory = new DirectoryCheckoutHttpAdapter(
                    WebClient.builder(),
                    directoryServer.baseUrl(),
                    "/api/v1/organizations/{organizationId}/addresses/{addressId}/checkout-resolution",
                    "",
                    3_000);

            CatalogVariantHttpAdapter catalog = new CatalogVariantHttpAdapter(
                    WebClient.builder(),
                    catalogServer.baseUrl(),
                    "/api/v1/catalog/checkout/variant-resolution",
                    "",
                    "COP",
                    "BASE",
                    3_000);

            InventoryReservationHttpAdapter inventory = new InventoryReservationHttpAdapter(
                    WebClient.builder(),
                    inventoryServer.baseUrl(),
                    "/api/v1/internal/reservations/{reservationId}/validation",
                    "",
                    3_000);

            Mono<FlowOutcome> flow = directory
                    .resolveCheckoutContext("tenant-core", "org-core", "addr-core", "CO")
                    .flatMap(context -> catalog.resolveVariant("tenant-core", null, "SKU-900")
                            .map(snapshot -> new FlowState(context, snapshot)))
                    .flatMap(state -> inventory
                            .validateReservation("tenant-core", "res-900", state.snapshot().sku(), 1)
                            .map(validation -> new FlowOutcome(state.context(), state.snapshot(), validation)));

            StepVerifier.create(flow)
                    .assertNext(outcome -> {
                        assertThat(outcome.context().policyActive()).isTrue();
                        assertThat(outcome.context().addressValid()).isTrue();
                        assertThat(outcome.snapshot().variantId()).isEqualTo("var-900");
                        assertThat(outcome.validation().reservationConfirmed()).isTrue();
                        assertThat(outcome.validation().commitableAvailable()).isTrue();
                    })
                    .verifyComplete();

            assertThat(directoryServer.lastPath()).isEqualTo("/api/v1/organizations/org-core/addresses/addr-core/checkout-resolution");
            assertThat(catalogServer.lastPath()).isEqualTo("/api/v1/catalog/checkout/variant-resolution");
            assertThat(inventoryServer.lastPath()).isEqualTo("/api/v1/internal/reservations/res-900/validation");
        }
    }

    private record FlowState(
            com.arka.order.application.port.out.directory.DirectoryCheckoutContext context,
            com.arka.order.application.port.out.external.CatalogVariantSnapshot snapshot) {}

    private record FlowOutcome(
            com.arka.order.application.port.out.directory.DirectoryCheckoutContext context,
            com.arka.order.application.port.out.external.CatalogVariantSnapshot snapshot,
            com.arka.order.application.port.out.external.InventoryReservationValidation validation) {}

    private static final class StubHttpServer implements AutoCloseable {

        private final HttpServer server;
        private final int status;
        private final String body;
        private final AtomicReference<String> lastPath = new AtomicReference<>();

        private StubHttpServer(int status, String body) throws IOException {
            this.status = status;
            this.body = body == null ? "" : body;
            this.server = HttpServer.create(new InetSocketAddress(0), 0);
            this.server.createContext("/", exchange -> {
                lastPath.set(exchange.getRequestURI().getPath());
                byte[] payload = this.body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(this.status, payload.length);
                exchange.getResponseBody().write(payload);
                exchange.close();
            });
            this.server.start();
        }

        static StubHttpServer responding(int status, String body) throws IOException {
            return new StubHttpServer(status, body);
        }

        String baseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        String lastPath() {
            return Objects.requireNonNull(lastPath.get(), "No request captured");
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}

