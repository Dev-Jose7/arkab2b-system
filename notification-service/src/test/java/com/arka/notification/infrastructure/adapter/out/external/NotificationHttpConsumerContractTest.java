package com.arka.notification.infrastructure.adapter.out.external;

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

class NotificationHttpConsumerContractTest {

    @Test
    void directoryRecipientResolverShouldRespectConsumerContract() throws Exception {
        try (StubHttpServer server = StubHttpServer.responding(
                200,
                """
                [
                  {"contactType":"EMAIL","value":"ops@arka.co","status":"ACTIVE"}
                ]
                """)) {
            DirectoryRecipientResolverHttpAdapter adapter = new DirectoryRecipientResolverHttpAdapter(
                    WebClient.builder(),
                    server.baseUrl(),
                    "/api/v1/organizations/{organizationId}/contacts",
                    "notification-token",
                    3_000);

            StepVerifier.create(adapter.resolve("tenant-1", "org-5", "EMAIL"))
                    .assertNext(resolution -> {
                        assertThat(resolution.recipientRef()).isEqualTo("org-5");
                        assertThat(resolution.channel()).isEqualTo("EMAIL");
                        assertThat(resolution.destination()).isEqualTo("ops@arka.co");
                        assertThat(resolution.active()).isTrue();
                    })
                    .verifyComplete();

            CapturedRequest request = server.lastRequest();
            assertThat(request.method()).isEqualTo("GET");
            assertThat(request.path()).isEqualTo("/api/v1/organizations/org-5/contacts");
            assertThat(request.header("Authorization")).isEqualTo("Bearer notification-token");
        }
    }

    @Test
    void orderContextLookupShouldRespectConsumerContract() throws Exception {
        try (StubHttpServer server = StubHttpServer.responding(
                200,
                """
                {"tenantId":"tenant-8","organizationId":"org-8","userId":"actor-8"}
                """)) {
            OrderContextLookupHttpAdapter adapter = new OrderContextLookupHttpAdapter(
                    WebClient.builder(),
                    server.baseUrl(),
                    "/api/v1/orders/{orderId}",
                    "/api/v1/carts/{cartId}",
                    "notification-token",
                    3_000);

            StepVerifier.create(adapter.resolveByOrderId("ord-8"))
                    .assertNext(context -> {
                        assertThat(context.tenantId()).isEqualTo("tenant-8");
                        assertThat(context.organizationId()).isEqualTo("org-8");
                        assertThat(context.actorId()).isEqualTo("actor-8");
                    })
                    .verifyComplete();

            CapturedRequest request = server.lastRequest();
            assertThat(request.method()).isEqualTo("GET");
            assertThat(request.path()).isEqualTo("/api/v1/orders/ord-8");
            assertThat(request.header("Authorization")).isEqualTo("Bearer notification-token");
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
