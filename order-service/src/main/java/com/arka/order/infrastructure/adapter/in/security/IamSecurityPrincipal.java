package com.arka.order.infrastructure.adapter.in.security;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public final class IamSecurityPrincipal {

    private final String userId;
    private final String tenantId;
    private final String organizationId;
    private final Set<String> roles;

    public IamSecurityPrincipal(String userId, String tenantId, String organizationId, Set<String> roles) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("organizationId is required");
        }
        this.userId = userId.trim();
        this.tenantId = tenantId.trim();
        this.organizationId = organizationId.trim();
        this.roles = normalizeRoles(roles);
    }

    public String userId() {
        return userId;
    }

    public String tenantId() {
        return tenantId;
    }

    public String organizationId() {
        return organizationId;
    }

    public Set<String> roles() {
        return roles;
    }

    public boolean isOrderAdmin() {
        return roles.contains("ORDER_ADMIN") || roles.contains("ROLE_ORDER_ADMIN") || roles.contains("ROLE_ARKA_ADMIN");
    }

    public static IamSecurityPrincipal fromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Authenticated principal is required");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof IamSecurityPrincipal iamPrincipal) {
            return iamPrincipal;
        }
        if (principal instanceof Jwt jwt) {
            return fromJwt(jwt, authentication.getAuthorities());
        }
        if (principal instanceof Principal p) {
            String identity = p.getName();
            return new IamSecurityPrincipal(identity, authentication.getName(), authentication.getName(), authorities(authentication.getAuthorities()));
        }
        return new IamSecurityPrincipal(authentication.getName(), authentication.getName(), authentication.getName(), authorities(authentication.getAuthorities()));
    }

    private static IamSecurityPrincipal fromJwt(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        String subject = claimString(jwt, "sub", jwt.getSubject());
        String tenantId = claimString(jwt, "tenant_id", claimString(jwt, "tenantId", ""));
        String organizationId = claimString(jwt, "organization_id", claimString(jwt, "organizationId", tenantId));
        return new IamSecurityPrincipal(subject, tenantId, organizationId, authorities(authorities));
    }

    private static String claimString(Jwt jwt, String claim, String fallback) {
        Object value = jwt.getClaims().get(claim);
        if (value == null) {
            return fallback;
        }
        String normalized = String.valueOf(value).trim();
        return normalized.isBlank() ? fallback : normalized;
    }

    private static Set<String> authorities(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (GrantedAuthority authority : authorities) {
            if (authority == null || authority.getAuthority() == null || authority.getAuthority().isBlank()) {
                continue;
            }
            values.add(authority.getAuthority().trim().toUpperCase());
        }
        return Collections.unmodifiableSet(values);
    }

    private Set<String> normalizeRoles(Set<String> rawRoles) {
        if (rawRoles == null || rawRoles.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String rawRole : rawRoles) {
            if (rawRole == null || rawRole.isBlank()) {
                continue;
            }
            normalized.add(rawRole.trim().toUpperCase());
        }
        return Collections.unmodifiableSet(normalized);
    }
}
