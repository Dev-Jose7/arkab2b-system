package com.arka.reporting.infrastructure.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.reporting.application.port.out.security.ActorContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SecurityActorContextProviderAdapterTest {

    @Test
    void shouldResolveActorContextFromJwtClaims() {
        SecurityActorContextProviderAdapter adapter = new SecurityActorContextProviderAdapter(
                true,
                "reporting-scheduler",
                "",
                "");

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "actor-1")
                .claim("organization_id", "organization-1")
                .claim("country_code", "co")
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                jwt,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_REPORTING_ADMIN")));

        Mono<ActorContext> contextMono = adapter.currentActor()
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        StepVerifier.create(contextMono)
                .assertNext(context -> {
                    assertEquals("actor-1", context.actorId());
                    assertEquals("organization-1", context.organizationId());
                    assertEquals("CO", context.countryCode());
                    assertTrue(context.admin());
                })
                .verifyComplete();
    }

    @Test
    void shouldFallbackToTrustedSchedulerActorWhenNoSecurityContext() {
        SecurityActorContextProviderAdapter adapter = new SecurityActorContextProviderAdapter(
                true,
                "reporting-scheduler",
                "",
                "");

        StepVerifier.create(adapter.currentActor())
                .assertNext(context -> {
                    assertEquals("reporting-scheduler", context.actorId());
                    assertTrue(context.trustedService());
                    assertTrue(context.admin());
                })
                .verifyComplete();
    }
}
