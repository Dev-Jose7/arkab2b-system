package com.arka.inventory.infrastructure.adapter.out.security;

import com.arka.inventory.application.port.out.security.ActorContext;
import com.arka.inventory.application.port.out.security.ActorContextProviderPort;
import com.arka.inventory.infrastructure.adapter.in.security.IamSecurityPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SecurityActorContextProviderAdapter implements ActorContextProviderPort {

    @Override
    public Mono<ActorContext> currentActor() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> toActorContext(context.getAuthentication()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Authenticated actor context is required")));
    }

    private ActorContext toActorContext(Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return new ActorContext(
                principal.userId(),
                principal.tenantId(),
                principal.isInventoryAdmin());
    }
}
