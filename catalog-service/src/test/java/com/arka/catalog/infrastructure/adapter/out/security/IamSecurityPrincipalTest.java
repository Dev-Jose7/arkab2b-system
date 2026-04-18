package com.arka.catalog.infrastructure.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.catalog.infrastructure.adapter.in.security.IamSecurityPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class IamSecurityPrincipalTest {

    @Test
    void shouldExtractActorTenantCountryAndRolesFromJwtAuthentication() {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-01T01:00:00Z"),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "actor-1",
                        "organization_id", "tenant-demo",
                        "country_code", "co"));

        var auth = new UsernamePasswordAuthenticationToken(
                jwt,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_CATALOG_ADMIN")));

        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(auth);

        assertEquals("actor-1", principal.actorId());
        assertEquals("tenant-demo", principal.tenantId());
        assertEquals("CO", principal.countryCode());
        assertTrue(principal.isCatalogAdmin());
    }
}
