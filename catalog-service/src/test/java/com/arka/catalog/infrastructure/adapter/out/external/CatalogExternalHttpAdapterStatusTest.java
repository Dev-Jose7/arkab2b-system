package com.arka.catalog.infrastructure.adapter.out.external;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CatalogExternalHttpAdapterStatusTest {

    @Test
    void identityLegitimacyShouldReturnFalseOn404AndErrorOnOther4xx5xx() {
        IdentityActorLegitimacyHttpAdapter notFound = legitimacyAdapter(HttpStatus.NOT_FOUND, "{\"error\":\"not-found\"}");
        StepVerifier.create(notFound.isLegitimate("actor-1", "organization-1"))
                .expectNext(false)
                .verifyComplete();

        IdentityActorLegitimacyHttpAdapter badRequest = legitimacyAdapter(HttpStatus.BAD_REQUEST, "{\"error\":\"bad\"}");
        StepVerifier.create(badRequest.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalArgumentException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unauthorized =
                legitimacyAdapter(HttpStatus.UNAUTHORIZED, "{\"error\":\"unauthorized\"}");
        StepVerifier.create(unauthorized.isLegitimate("actor-1", "organization-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter forbidden = legitimacyAdapter(HttpStatus.FORBIDDEN, "{\"error\":\"forbidden\"}");
        StepVerifier.create(forbidden.isLegitimate("actor-1", "organization-1"))
                .expectError(SecurityException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter conflict = legitimacyAdapter(HttpStatus.CONFLICT, "{\"error\":\"conflict\"}");
        StepVerifier.create(conflict.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter unprocessable =
                legitimacyAdapter(HttpStatus.UNPROCESSABLE_ENTITY, "{\"error\":\"unprocessable\"}");
        StepVerifier.create(unprocessable.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalStateException.class)
                .verify();

        IdentityActorLegitimacyHttpAdapter serverError =
                legitimacyAdapter(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"boom\"}");
        StepVerifier.create(serverError.isLegitimate("actor-1", "organization-1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void regionalPolicyLookupShouldTreat404AsAbsenceAndDifferentiate4xx5xx() {
        DirectoryRegionalPolicyContextHttpAdapter ok = policyAdapter(
                HttpStatus.OK,
                """
                {"policyId":"policy-1","currencyCode":"usd","status":"ACTIVE"}
                """,
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(ok.resolveForOrganization("organization-1", "co"))
                .assertNext(context -> {
                    assertEquals("policy-1", context.policyReference());
                    assertEquals("CO", context.countryCode());
                    assertEquals("USD", context.currencyCode());
                })
                .verifyComplete();

        DirectoryRegionalPolicyContextHttpAdapter notFound = policyAdapter(
                HttpStatus.NOT_FOUND,
                "{\"error\":\"not-found\"}",
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(notFound.resolveForOrganization("organization-1", "CO")).verifyComplete();

        DirectoryRegionalPolicyContextHttpAdapter badRequest = policyAdapter(
                HttpStatus.BAD_REQUEST,
                "{\"error\":\"bad\"}",
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(badRequest.resolveForOrganization("organization-1", "CO"))
                .expectError(IllegalArgumentException.class)
                .verify();

        DirectoryRegionalPolicyContextHttpAdapter unauthorized = policyAdapter(
                HttpStatus.UNAUTHORIZED,
                "{\"error\":\"unauthorized\"}",
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(unauthorized.resolveForOrganization("organization-1", "CO"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryRegionalPolicyContextHttpAdapter forbidden = policyAdapter(
                HttpStatus.FORBIDDEN,
                "{\"error\":\"forbidden\"}",
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(forbidden.resolveForOrganization("organization-1", "CO"))
                .expectError(SecurityException.class)
                .verify();

        DirectoryRegionalPolicyContextHttpAdapter conflict = policyAdapter(
                HttpStatus.CONFLICT,
                "{\"error\":\"conflict\"}",
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(conflict.resolveForOrganization("organization-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryRegionalPolicyContextHttpAdapter unprocessable = policyAdapter(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "{\"error\":\"unprocessable\"}",
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(unprocessable.resolveForOrganization("organization-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();

        DirectoryRegionalPolicyContextHttpAdapter serverError = policyAdapter(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "{\"error\":\"boom\"}",
                "/api/v1/organizations/{organizationId}/country-policies/{countryCode}");
        StepVerifier.create(serverError.resolveForOrganization("organization-1", "CO"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldUseDefaultCountryPolicyPathWhenConfiguredPathIsBlank() {
        AtomicReference<String> capturedPath = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            capturedPath.set(request.url().getPath());
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"error\":\"not-found\"}")
                    .build());
        };

        DirectoryRegionalPolicyContextHttpAdapter adapter = new DirectoryRegionalPolicyContextHttpAdapter(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "http://directory-service",
                "",
                "",
                "COP",
                3_000);

        StepVerifier.create(adapter.resolveForOrganization("organization-1", "CO")).verifyComplete();
        assertEquals("/api/v1/internal/organizations/organization-1/country-policies/CO", capturedPath.get());
    }

    private IdentityActorLegitimacyHttpAdapter legitimacyAdapter(HttpStatus status, String body) {
        return new IdentityActorLegitimacyHttpAdapter(
                builder(status, body),
                "http://identity-access-service",
                "/api/v1/admin/iam/users/{actorId}/permissions",
                "",
                3_000);
    }

    private DirectoryRegionalPolicyContextHttpAdapter policyAdapter(HttpStatus status, String body, String path) {
        return new DirectoryRegionalPolicyContextHttpAdapter(
                builder(status, body),
                "http://directory-service",
                path,
                "",
                "COP",
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
