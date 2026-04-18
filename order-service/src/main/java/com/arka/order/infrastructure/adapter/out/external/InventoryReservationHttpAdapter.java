package com.arka.order.infrastructure.adapter.out.external;

import com.arka.order.application.port.out.external.InventoryReservationPort;
import com.arka.order.application.port.out.external.InventoryReservationValidation;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryReservationHttpAdapter implements InventoryReservationPort {

    private final WebClient webClient;
    private final String path;
    private final String serviceToken;
    private final Duration timeout;

    public InventoryReservationHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.inventory.base-url:http://inventory-service:8080}") String baseUrl,
            @Value("${app.external.inventory.reservation-validation-path:/api/v1/internal/reservations/{reservationId}/validation}") String path,
            @Value("${app.external.inventory.service-token:}") String serviceToken,
            @Value("${app.external.inventory.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.path = path;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<InventoryReservationValidation> validateReservation(String tenantId, String reservationId, String sku, int qty) {
        if (tenantId == null || tenantId.isBlank() || reservationId == null || reservationId.isBlank()
                || sku == null || sku.isBlank() || qty <= 0) {
            return Mono.just(new InventoryReservationValidation(reservationId, sku, qty, false, false));
        }
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("tenantId", tenantId.trim())
                        .queryParam("sku", sku.trim().toUpperCase())
                        .queryParam("qty", qty)
                        .build(reservationId.trim()))
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class).map(this::toValidation);
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.just(new InventoryReservationValidation(
                                reservationId.trim(),
                                sku.trim().toUpperCase(),
                                qty,
                                false,
                                false));
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, reservationId, sku, qty, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Inventory reservation validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String reservationId, String sku, int qty, String body) {
        String normalizedReservationId = reservationId == null ? "" : reservationId.trim();
        String normalizedSku = sku == null ? "" : sku.trim().toUpperCase();
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Inventory reservation validation request rejected (400). reservationId=" + normalizedReservationId
                            + " sku=" + normalizedSku + " qty=" + qty + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Inventory reservation validation unauthorized/forbidden. status=" + status + " reservationId="
                            + normalizedReservationId + " sku=" + normalizedSku + " qty=" + qty + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Inventory reservation validation conflict (409). reservationId=" + normalizedReservationId
                            + " sku=" + normalizedSku + " qty=" + qty + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Inventory reservation validation semantic error (422). reservationId=" + normalizedReservationId
                            + " sku=" + normalizedSku + " qty=" + qty + " body=" + body);
            default -> new IllegalStateException(
                    "Inventory reservation validation client error. status=" + status + " reservationId="
                            + normalizedReservationId + " sku=" + normalizedSku + " qty=" + qty + " body=" + body);
        };
    }

    private InventoryReservationValidation toValidation(JsonNode body) {
        String reservationId = text(body, "reservationId");
        String sku = text(body, "sku");
        int qty = body.path("qty").asInt(0);
        boolean reservationConfirmed = body.path("reservationConfirmed").asBoolean(false);
        boolean commitableAvailable = body.path("commitableAvailable").asBoolean(false);
        return new InventoryReservationValidation(
                reservationId,
                sku,
                qty,
                reservationConfirmed,
                commitableAvailable);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }
}
