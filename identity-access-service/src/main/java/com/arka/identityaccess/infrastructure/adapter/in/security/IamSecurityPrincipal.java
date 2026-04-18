package com.arka.identityaccess.infrastructure.adapter.in.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.Authentication;

public final class IamSecurityPrincipal {

    private final String userId;
    private final String sessionId;
    private final String email;
    private final String organizationId;
    private final String countryCode;
    private final Set<String> roles;

    public IamSecurityPrincipal(String userId, String sessionId, String email, Set<String> roles) {
        this(userId, sessionId, email, null, null, roles);
    }

    public IamSecurityPrincipal(String userId, String sessionId, String email, String organizationId, Set<String> roles) {
        this(userId, sessionId, email, organizationId, null, roles);
    }

    public IamSecurityPrincipal(
            String userId,
            String sessionId,
            String email,
            String organizationId,
            String countryCode,
            Set<String> roles) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }
        this.userId = userId.trim();
        this.sessionId = sessionId.trim();
        this.email = email == null ? "" : email.trim();
        this.organizationId = organizationId == null ? "" : organizationId.trim();
        this.countryCode = countryCode == null ? "" : countryCode.trim().toUpperCase();
        this.roles = normalizeRoles(roles);
    }

    public String userId() {
        return userId;
    }

    public String sessionId() {
        return sessionId;
    }

    public String email() {
        return email;
    }

    public String organizationId() {
        return organizationId;
    }

    public String countryCode() {
        return countryCode;
    }

    public Set<String> roles() {
        return roles;
    }

    public static IamSecurityPrincipal fromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authenticated principal is required");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof IamSecurityPrincipal iamPrincipal) {
            return iamPrincipal;
        }
        throw new IllegalArgumentException("Unsupported authenticated principal type");
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
