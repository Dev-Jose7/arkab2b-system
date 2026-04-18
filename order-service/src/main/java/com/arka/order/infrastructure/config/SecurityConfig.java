package com.arka.order.infrastructure.config;

import com.arka.order.infrastructure.adapter.in.security.JsonAccessDeniedHandler;
import com.arka.order.infrastructure.adapter.in.security.JsonAuthenticationEntryPoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET,
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(this::toAuthentication)))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${app.security.jwt.public-key-path:classpath:keys/dev-public.pem}") String publicKeyPath,
            ResourceLoader resourceLoader) {
        RSAPublicKey publicKey = loadPublicKey(publicKeyPath, resourceLoader);
        return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
    }

    private Mono<AbstractAuthenticationToken> toAuthentication(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
        return Mono.just(new UsernamePasswordAuthenticationToken(jwt, jwt.getTokenValue(), authorities));
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

    private RSAPublicKey loadPublicKey(String location, ResourceLoader resourceLoader) {
        try {
            Resource resource = resourceLoader.getResource(location);
            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to load JWT public key from " + location, exception);
        }
    }
}
