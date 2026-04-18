package com.arka.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.security.SecurityPrincipalMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IamSecurityPrincipalClaimsTest {

    @Test
    void shouldPreserveOrganizationAndCountryClaims() {
        SecurityPrincipalMapper mapper = new SecurityPrincipalMapper();

        IamSecurityPrincipal principal = mapper.toPrincipal(
                "actor-1",
                "sess-1",
                "actor@arka.com",
                Set.of("ORG_ADMIN"),
                "org-1",
                "co");

        assertEquals("org-1", principal.organizationId());
        assertEquals("CO", principal.countryCode());
    }
}
