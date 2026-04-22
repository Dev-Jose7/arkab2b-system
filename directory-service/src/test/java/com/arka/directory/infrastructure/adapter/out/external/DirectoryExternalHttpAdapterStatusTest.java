package com.arka.directory.infrastructure.adapter.out.external;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DirectoryExternalHttpAdapterStatusTest {

    @Test
    void geoValidationShouldDifferentiate4xxAnd5xx() {
        GeoValidationHttpAdapter ok = geoAdapter(HttpStatus.OK, "{\"valid\":true}");
        StepVerifier.create(ok.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectNext(true)
                .verifyComplete();

        GeoValidationHttpAdapter badRequest = geoAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        GeoValidationHttpAdapter unauthorized = geoAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectError(SecurityException.class)
                .verify();

        GeoValidationHttpAdapter forbidden = geoAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectError(SecurityException.class)
                .verify();

        GeoValidationHttpAdapter notFound = geoAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectError(IllegalStateException.class)
                .verify();

        GeoValidationHttpAdapter conflict = geoAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectError(IllegalStateException.class)
                .verify();

        GeoValidationHttpAdapter unprocessable = geoAdapter(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectError(IllegalStateException.class)
                .verify();

        GeoValidationHttpAdapter serverError = geoAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.isAddressValid("CO", "Bogota", "110111", "Street 1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void taxValidationShouldDifferentiate4xxAnd5xx() {
        TaxValidationHttpAdapter ok = taxAdapter(HttpStatus.OK, "{\"valid\":true}");
        StepVerifier.create(ok.isTaxIdValid("CO", "NIT", "900123123"))
                .expectNext(true)
                .verifyComplete();

        TaxValidationHttpAdapter badRequest = taxAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.isTaxIdValid("CO", "NIT", "900123123"))
                .expectError(IllegalArgumentException.class)
                .verify();

        TaxValidationHttpAdapter unauthorized = taxAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.isTaxIdValid("CO", "NIT", "900123123"))
                .expectError(SecurityException.class)
                .verify();

        TaxValidationHttpAdapter forbidden = taxAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.isTaxIdValid("CO", "NIT", "900123123"))
                .expectError(SecurityException.class)
                .verify();

        TaxValidationHttpAdapter notFound = taxAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.isTaxIdValid("CO", "NIT", "900123123"))
                .expectError(IllegalStateException.class)
                .verify();

        TaxValidationHttpAdapter conflict = taxAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.isTaxIdValid("CO", "NIT", "900123123"))
                .expectError(IllegalStateException.class)
                .verify();

        TaxValidationHttpAdapter unprocessable = taxAdapter(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.isTaxIdValid("CO", "NIT", "900123123"))
                .expectError(IllegalStateException.class)
                .verify();

        TaxValidationHttpAdapter serverError = taxAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.isTaxIdValid("CO", "NIT", "900123123"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void identityLegitimacyShouldReturnFalseOn404AndErrorOnOther4xx5xx() {
        IdentityActorLegitimacyHttpAdapter notFound = identityAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.isLegitimate("actor-1"))
                .expectNext(false)
                .verifyComplete();

        IdentityActorLegitimacyHttpAdapter badRequest = identityAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.isLegitimate("actor-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unauthorized =
                identityAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.isLegitimate("actor-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter forbidden = identityAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.isLegitimate("actor-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter conflict = identityAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.isLegitimate("actor-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unprocessable =
                identityAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.isLegitimate("actor-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter serverError =
                identityAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.isLegitimate("actor-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    private GeoValidationHttpAdapter geoAdapter(HttpStatus status, String body) {
        return new GeoValidationHttpAdapter(
                builder(status, body),
                "http://validation-service",
                true,
                "/api/v1/validation/geo/address",
                "",
                3_000);
    }

    private TaxValidationHttpAdapter taxAdapter(HttpStatus status, String body) {
        return new TaxValidationHttpAdapter(
                builder(status, body),
                "http://validation-service",
                true,
                "/api/v1/validation/tax-id",
                "",
                3_000);
    }

    private IdentityActorLegitimacyHttpAdapter identityAdapter(HttpStatus status, String body) {
        return new IdentityActorLegitimacyHttpAdapter(
                builder(status, body),
                "http://identity-access-service",
                "/api/v1/admin/iam/users/{actorId}/permissions",
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
