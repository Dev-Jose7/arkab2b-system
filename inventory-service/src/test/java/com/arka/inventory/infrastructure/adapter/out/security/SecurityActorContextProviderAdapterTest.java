package com.arka.inventory.infrastructure.adapter.out.security;

import com.arka.inventory.application.port.out.security.ActorContext;
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
                .claim("tenant_id", "tenant-1")
                .build();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                jwt,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_INVENTORY_ADMIN")));

        Mono<ActorContext> contextMono = adapter.currentActor()
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        StepVerifier.create(contextMono)
                .assertNext(context -> {
                    assertEquals("actor-1", context.userId());
                    assertEquals("tenant-1", context.tenantId());
                    assertTrue(context.inventoryAdmin());
                })
                .verifyComplete();
    }
}
