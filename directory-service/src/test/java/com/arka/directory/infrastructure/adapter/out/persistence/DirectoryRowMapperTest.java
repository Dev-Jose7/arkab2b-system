package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.domain.countrypolicy.enumtype.WeekStartsOn;
import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationStatus;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationCountryPolicyRow;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationRow;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectoryRowMapperTest {

    private final DirectoryRowMapper mapper = new DirectoryRowMapper();

    @Test
    void organizationRoundTripMappingKeepsCoreFields() {
        Organization organization = Organization.rehydrate(
                OrganizationId.of("org-1"),
                "Acme Corp",
                "Acme",
                CountryCode.of("CO"),
                "COP",
                "America/Bogota",
                "SMB",
                OrganizationStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"));

        OrganizationRow row = mapper.toRow(organization);
        Organization restored = mapper.toDomain(row);

        assertEquals("org-1", restored.id().value());
        assertEquals("ACTIVE", restored.status().name());
    }

    @Test
    void countryPolicyRoundTripMappingKeepsVersionAndStatus() {
        CountryPolicy policy = CountryPolicy.rehydrate(
                "policy-1",
                OrganizationId.of("org-1"),
                CountryCode.of("CO"),
                PolicyVersion.of(4),
                "COP",
                WeekStartsOn.MONDAY,
                "18:00",
                "America/Bogota",
                30,
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                CountryPolicyStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"));

        OrganizationCountryPolicyRow row = mapper.toRow(policy);
        CountryPolicy restored = mapper.toDomain(row);

        assertEquals("org-1", restored.organizationId().value());
        assertEquals("CO", restored.countryCode().value());
        assertEquals(4L, restored.policyVersion().value());
        assertEquals(CountryPolicyStatus.ACTIVE, restored.status());
    }
}
