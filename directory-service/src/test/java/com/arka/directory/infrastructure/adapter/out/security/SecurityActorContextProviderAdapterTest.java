package com.arka.directory.infrastructure.adapter.out.security;

import com.arka.directory.application.port.out.security.ActorContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityActorContextProviderAdapterTest {

    @Test
    void resolvesActorContextFromJwtClaims() {
        SecurityActorContextProviderAdapter adapter = new SecurityActorContextProviderAdapter();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "actor-1")
                .claim("organization_id", "org-1")
                .claim("country_code", "co")
                .build();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                jwt,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_DIRECTORY_ADMIN")));

        Mono<ActorContext> contextMono = adapter.currentActor()
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        StepVerifier.create(contextMono)
                .assertNext(context -> {
                    assertEquals("actor-1", context.userId());
                    assertEquals("org-1", context.organizationId());
                    assertEquals("CO", context.countryCode());
                    assertTrue(context.directoryAdmin());
                })
                .verifyComplete();
    }
}
