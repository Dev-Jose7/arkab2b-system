package com.arka.inventory.infrastructure.adapter.in.security;

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
    private final String organizationId;
    private final Set<String> roles;

    public IamSecurityPrincipal(String userId, String organizationId, Set<String> roles) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        this.userId = userId.trim();
        this.organizationId = organizationId == null ? "" : organizationId.trim();
        this.roles = normalizeRoles(roles);
    }

    public String userId() {
        return userId;
    }

    public String organizationId() {
        return organizationId;
    }

    public Set<String> roles() {
        return roles;
    }

    public boolean isInventoryAdmin() {
        return roles.contains("INVENTORY_ADMIN") || roles.contains("ROLE_INVENTORY_ADMIN");
    }

    public boolean isInternalActor() {
        return roles.contains("INTERNAL_ACTOR") || roles.contains("ROLE_INTERNAL_ACTOR");
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
            return new IamSecurityPrincipal(
                    p.getName(),
                    authentication.getName(),
                    authorities(authentication.getAuthorities()));
        }
        return new IamSecurityPrincipal(
                authentication.getName(),
                authentication.getName(),
                authorities(authentication.getAuthorities()));
    }

    private static IamSecurityPrincipal fromJwt(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        String subject = jwt.getSubject();
        String organizationId = claimString(jwt, "organization_id", claimString(jwt, "organization_id", ""));
        return new IamSecurityPrincipal(subject, organizationId, authorities(authorities));
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
