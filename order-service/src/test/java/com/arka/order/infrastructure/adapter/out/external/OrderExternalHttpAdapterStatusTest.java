package com.arka.order.infrastructure.adapter.out.external;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderExternalHttpAdapterStatusTest {

    @Test
    void identityLegitimacyShouldReturnFalseOn404AndErrorOnOther4xx() {
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
    void inventoryReservationShouldTreat404AsInvalidAndOther4xxAsErrors() {
        InventoryReservationHttpAdapter notFound = reservationAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.validateReservation("organization-1", "res-1", "SKU-1", 2))
                .assertNext(result -> {
                    assertFalse(result.reservationConfirmed());
                    assertFalse(result.commitableAvailable());
                })
                .verifyComplete();

        InventoryReservationHttpAdapter badRequest = reservationAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.validateReservation("organization-1", "res-1", "SKU-1", 2))
                .expectError(IllegalArgumentException.class)
                .verify();

        InventoryReservationHttpAdapter unauthorized =
                reservationAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.validateReservation("organization-1", "res-1", "SKU-1", 2))
                .expectError(SecurityException.class)
                .verify();

        InventoryReservationHttpAdapter forbidden =
                reservationAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.validateReservation("organization-1", "res-1", "SKU-1", 2))
                .expectError(SecurityException.class)
                .verify();

        InventoryReservationHttpAdapter conflict =
                reservationAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.validateReservation("organization-1", "res-1", "SKU-1", 2))
                .expectError(IllegalStateException.class)
                .verify();

        InventoryReservationHttpAdapter unprocessable =
                reservationAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.validateReservation("organization-1", "res-1", "SKU-1", 2))
                .expectError(IllegalStateException.class)
                .verify();

        InventoryReservationHttpAdapter serverError =
                reservationAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.validateReservation("organization-1", "res-1", "SKU-1", 2))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void directoryCheckoutShouldErrorAcross4xxAnd5xx() {
        DirectoryCheckoutHttpAdapter ok = checkoutAdapter(
                HttpStatus.OK,
                """
                {
                  "resolutionStatus":"RESOLVED",
                  "address":{"countryCode":"CO","validationStatus":"VALID"},
                  "countryPolicy":{"policyVersion":9,"currencyCode":"COP","status":"ACTIVE"}
                }
                """);
        StepVerifier.create(ok.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .assertNext(context -> {
                    assertTrue(context.policyActive());
                    assertTrue(context.addressValid());
                })
                .verifyComplete();

        DirectoryCheckoutHttpAdapter notFound = checkoutAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .expectError(IllegalArgumentException.class)
                .verify();

        DirectoryCheckoutHttpAdapter badRequest = checkoutAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .expectError(IllegalArgumentException.class)
                .verify();

        DirectoryCheckoutHttpAdapter unauthorized = checkoutAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryCheckoutHttpAdapter forbidden = checkoutAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryCheckoutHttpAdapter conflict = checkoutAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryCheckoutHttpAdapter unprocessable =
                checkoutAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryCheckoutHttpAdapter serverError =
                checkoutAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.resolveCheckoutContext("organization-1", "addr-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    private IdentityActorLegitimacyHttpAdapter legitimacyAdapter(HttpStatus status, String body) {
        return new IdentityActorLegitimacyHttpAdapter(
                builder(status, body),
                "http://identity-access-service",
                "/api/v1/admin/iam/users/{actorId}/permissions",
                3_000);
    }

    private InventoryReservationHttpAdapter reservationAdapter(HttpStatus status, String body) {
        return new InventoryReservationHttpAdapter(
                builder(status, body),
                "http://inventory-service",
                "/api/v1/internal/reservations/{reservationId}/validation",
                3_000);
    }

    private DirectoryCheckoutHttpAdapter checkoutAdapter(HttpStatus status, String body) {
        return new DirectoryCheckoutHttpAdapter(
                builder(status, body),
                "http://directory-service",
                "/api/v1/organizations/{organizationId}/addresses/{addressId}/checkout-resolution",
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
