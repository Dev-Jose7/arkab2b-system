package com.arka.order.infrastructure.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.order.infrastructure.adapter.in.security.IamSecurityPrincipal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class IamSecurityPrincipalTest {

    @Test
    void shouldExtractOrganizationAndRolesFromJwtAuthentication() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("organization_id", "org-1")
                .subject("user-1")
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                jwt,
                jwt.getTokenValue(),
                List.of(new SimpleGrantedAuthority("ROLE_ORDER_ADMIN")));

        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);

        assertEquals("user-1", principal.userId());
        assertEquals("org-1", principal.organizationId());
        assertTrue(principal.isOrderAdmin());
    }
}
