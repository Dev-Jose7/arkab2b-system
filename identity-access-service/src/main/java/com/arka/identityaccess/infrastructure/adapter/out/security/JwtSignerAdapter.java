package com.arka.identityaccess.infrastructure.adapter.out.security;

import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtSignerAdapter implements JwtSigningPort {

    private final JwtRsaKeyProvider jwtRsaKeyProvider;
    private final String issuer;
    private final String audience;

    public JwtSignerAdapter(
            JwtRsaKeyProvider jwtRsaKeyProvider,
            @Value("${app.security.jwt.issuer:identity-access-service}") String issuer,
            @Value("${app.security.jwt.audience:arka-b2b}") String audience) {
        this.jwtRsaKeyProvider = jwtRsaKeyProvider;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    public Mono<String> signAccessToken(SessionAggregate session, AccessProfile accessProfile) {
        return Mono.fromSupplier(() -> encodeAccessToken(session, accessProfile));
    }

    @Override
    public Mono<String> signRefreshToken(SessionAggregate session) {
        return Mono.fromSupplier(() -> encodeRefreshToken(session));
    }

    @Override
    public Mono<String> signServiceToken(ServiceTokenClaims claims) {
        return Mono.fromSupplier(() -> encodeServiceToken(claims));
    }

    private String encodeAccessToken(SessionAggregate session, AccessProfile accessProfile) {
        if (accessProfile == null) {
            throw new IllegalStateException("Access profile is required for access token signing");
        }
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(session.userId().value())
                .jwtID(session.accessJti().value())
                .issueTime(Date.from(session.timestamps().createdAt()))
                .expirationTime(Date.from(session.timestamps().accessTokenExpiresAt()))
                .claim("sid", session.id().value())
                .claim("typ", "access")
                .claim("email", normalizeEmail(accessProfile.email().value()))
                .claim("roles", normalizeRoles(accessProfile.roleCodeValues()))
                .claim("permissions", normalizePermissions(accessProfile.permissionCodeValues()))
                .build();
        return sign(claimsSet);
    }

    private String encodeRefreshToken(SessionAggregate session) {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(session.userId().value())
                .jwtID(session.refreshJti().value())
                .issueTime(Date.from(session.timestamps().createdAt()))
                .expirationTime(Date.from(session.timestamps().refreshTokenExpiresAt()))
                .claim("sid", session.id().value())
                .claim("typ", "refresh")
                .build();
        return sign(claimsSet);
    }

    private String encodeServiceToken(ServiceTokenClaims claims) {
        if (claims == null) {
            throw new IllegalStateException("Service token claims are required");
        }
        if (claims.clientId() == null || claims.clientId().isBlank()) {
            throw new IllegalStateException("Service token clientId is required");
        }
        String normalizedAudience = claims.audience() == null || claims.audience().isBlank()
                ? audience
                : claims.audience().trim();
        long ttlSeconds = claims.ttlSeconds() <= 0 ? 300L : claims.ttlSeconds();
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + (ttlSeconds * 1000L));

        LinkedHashSet<String> normalizedScopes = new LinkedHashSet<>();
        normalizedScopes.add("service");
        if (claims.scopes() != null) {
            for (String scope : claims.scopes()) {
                if (scope == null || scope.isBlank()) {
                    continue;
                }
                normalizedScopes.add(scope.trim());
            }
        }

        LinkedHashSet<String> normalizedRoles = new LinkedHashSet<>();
        normalizedRoles.add("TRUSTED_SERVICE");
        if (claims.roles() != null) {
            normalizedRoles.addAll(normalizeRoles(claims.roles()));
        }

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(normalizedAudience)
                .subject("svc:" + claims.clientId().trim())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(issuedAt)
                .expirationTime(expiresAt)
                .claim("typ", "service")
                .claim("client_id", claims.clientId().trim())
                .claim("scope", String.join(" ", normalizedScopes))
                .claim("scp", normalizedScopes)
                .claim("permissions", normalizedScopes)
                .claim("roles", normalizedRoles);

        if (claims.organizationId() != null && !claims.organizationId().isBlank()) {
            builder.claim("organization_id", claims.organizationId().trim());
            builder.claim("organizationId", claims.organizationId().trim());
        }
        String organizationId = claims.organizationId();
        if ((organizationId == null || organizationId.isBlank())
                && claims.organizationId() != null
                && !claims.organizationId().isBlank()) {
            organizationId = claims.organizationId();
        }
        if (organizationId != null && !organizationId.isBlank()) {
            builder.claim("organization_id", organizationId.trim());
            builder.claim("organizationId", organizationId.trim());
        }
        if (claims.countryCode() != null && !claims.countryCode().isBlank()) {
            String countryCode = claims.countryCode().trim().toUpperCase(Locale.ROOT);
            builder.claim("country_code", countryCode);
            builder.claim("countryCode", countryCode);
        }
        return sign(builder.build());
    }

    private String sign(JWTClaimsSet claimsSet) {
        try {
            JWSSigner signer = new RSASSASigner(jwtRsaKeyProvider.privateKey());
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .type(JOSEObjectType.JWT)
                            .keyID(jwtRsaKeyProvider.keyId())
                            .build(),
                    claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to sign JWT token", exception);
        }
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.trim().toUpperCase())
                .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private Set<String> normalizePermissions(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Set.of();
        }
        return permissions.stream()
                .filter(permission -> permission != null && !permission.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email is required for access token claim");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
