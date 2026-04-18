package com.arka.reporting.infrastructure.adapter.out.security;

import com.arka.reporting.application.port.out.security.ActorContext;
import com.arka.reporting.application.port.out.security.ActorContextProviderPort;
import com.arka.reporting.infrastructure.adapter.in.security.IamSecurityPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SecurityActorContextProviderAdapter implements ActorContextProviderPort {

    private final boolean fallbackEnabled;
    private final ActorContext fallbackActorContext;

    public SecurityActorContextProviderAdapter(
            @Value("${app.security.actor.fallback-enabled:false}") boolean fallbackEnabled,
            @Value("${app.security.actor.fallback-actor-id:reporting-scheduler}") String fallbackActorId,
            @Value("${app.security.actor.fallback-organization-id:${app.security.actor.fallback-organization-id:}}")
                    String fallbackOrganizationId,
            @Value("${app.security.actor.fallback-country-code:}") String fallbackCountryCode) {
        this.fallbackEnabled = fallbackEnabled;
        this.fallbackActorContext = new ActorContext(
                fallbackActorId,
                fallbackOrganizationId,
                fallbackCountryCode,
                true,
                true);
    }

    @Override
    public Mono<ActorContext> currentActor() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> toActorContext(context.getAuthentication()))
                .switchIfEmpty(fallbackEnabled
                        ? Mono.just(fallbackActorContext)
                        : Mono.error(new IllegalStateException("Authenticated actor context is required")));
    }

    private ActorContext toActorContext(Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return new ActorContext(
                principal.actorId(),
                principal.organizationId(),
                principal.countryCode(),
                principal.isReportingAdmin(),
                principal.isTrustedService());
    }
}
