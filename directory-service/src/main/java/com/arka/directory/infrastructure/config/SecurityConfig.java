package com.arka.directory.infrastructure.config;

import com.arka.directory.infrastructure.adapter.in.security.JsonAccessDeniedHandler;
import com.arka.directory.infrastructure.adapter.in.security.JsonAuthenticationEntryPoint;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final boolean allowSwaggerIframe;

    public SecurityConfig(Environment environment) {
        this.allowSwaggerIframe = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "local".equalsIgnoreCase(profile));
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler) {
        ServerHttpSecurity security = http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        if (allowSwaggerIframe) {
            security.headers(headers -> headers.frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable));
        }

        return security
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET,
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v1/api-docs/**")
                        .permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(this::toAuthentication)))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${app.security.jwt.jwks-uri:http://localhost:8081/.well-known/jwks.json}") String jwksUri,
            @Value("${app.security.jwt.issuer:identity-access-service}") String issuer,
            @Value("${app.security.jwt.allowed-audiences:arka-b2b,${spring.application.name}}") List<String> allowedAudiences,
            @Value("${app.security.jwt.clock-skew-seconds:60}") long clockSkewSeconds) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwksUri).build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(JwtValidators.createDefaultWithIssuer(issuer));

        JwtTimestampValidator timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(Math.max(0L, clockSkewSeconds)));
        validators.add(timestampValidator);

        validators.add(new JwtClaimValidator<List<String>>("aud", aud -> {
            if (aud == null || aud.isEmpty()) {
                return false;
            }
            return aud.stream().anyMatch(candidate -> allowedAudiences.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .anyMatch(allowed -> allowed.equals(candidate)));
        }));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }

    private Mono<AbstractAuthenticationToken> toAuthentication(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(jwt, "n/a", authorities);
        authentication.setDetails(jwt.getClaims());
        return Mono.just(authentication);
    }

    private Set<String> extractAuthorities(Jwt jwt) {
        LinkedHashSet<String> authorities = new LinkedHashSet<>();
        authorities.addAll(extractStringClaimValues(jwt.getClaim("permissions")));

        for (String role : extractStringClaimValues(jwt.getClaim("roles"))) {
            String normalizedRole = role.toUpperCase();
            authorities.add(normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole);
        }

        authorities.addAll(extractScopeValues(jwt.getClaimAsString("scope")));
        authorities.addAll(extractStringClaimValues(jwt.getClaim("scp")));
        return authorities;
    }

    private Set<String> extractScopeValues(String scopeClaim) {
        if (scopeClaim == null || scopeClaim.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String value : scopeClaim.split("\\s+")) {
            String normalized = value == null ? "" : value.trim();
            if (!normalized.isBlank()) {
                values.add(normalized);
            }
        }
        return values;
    }

    private Set<String> extractStringClaimValues(Object claimValue) {
        if (claimValue == null) {
            return Set.of();
        }
        if (claimValue instanceof Collection<?> collection) {
            LinkedHashSet<String> values = new LinkedHashSet<>();
            for (Object item : collection) {
                String normalized = item == null ? "" : String.valueOf(item).trim();
                if (!normalized.isBlank()) {
                    values.add(normalized);
                }
            }
            return values;
        }
        String raw = String.valueOf(claimValue);
        if (raw.isBlank()) {
            return Set.of();
        }
        List<String> parts = new ArrayList<>();
        if (raw.contains(",")) {
            parts.addAll(List.of(raw.split(",")));
        } else {
            parts.add(raw);
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String part : parts) {
            String normalized = part == null ? "" : part.trim();
            if (!normalized.isBlank()) {
                values.add(normalized);
            }
        }
        return values;
    }
}
