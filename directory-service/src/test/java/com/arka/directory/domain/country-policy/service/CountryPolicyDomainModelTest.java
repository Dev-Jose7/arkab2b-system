package com.arka.directory.domain.countrypolicy.service;

import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.domain.countrypolicy.enumtype.WeekStartsOn;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.arka.directory.domain.organizationcontext.valueobject.RegionalPolicyResolution;
import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountryPolicyDomainModelTest {

    @Test
    void configureBuildsNextVersionAndEmitsConfiguredEvent() {
        CountryPolicy policy = CountryPolicy.configure(
                "policy-1",
                OrganizationId.of("org-1"),
                CountryCode.of("CO"),
                PolicyVersion.of(2),
                "COP",
                WeekStartsOn.MONDAY,
                "18:00",
                "America/Bogota",
                30,
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals(3L, policy.policyVersion().value());
        assertEquals(CountryPolicyStatus.ACTIVE, policy.status());
        List<DomainEvent> events = policy.pullDomainEvents();
        assertEquals(1, events.size());
        assertEquals("RegionalPolicyConfigured", events.getFirst().eventType());
    }

    @Test
    void applyToOperationReturnsResolutionAndEmitsEvent() {
        CountryPolicy policy = CountryPolicy.configure(
                "policy-1",
                OrganizationId.of("org-1"),
                CountryCode.of("CO"),
                null,
                "COP",
                WeekStartsOn.MONDAY,
                "18:00",
                "America/Bogota",
                30,
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
        policy.pullDomainEvents();

        RegionalPolicyResolution resolution = policy.applyToOperation(
                "CHECKOUT",
                "actor-1",
                Instant.parse("2026-01-01T01:00:00Z"));

        assertEquals("org-1", resolution.organizationId());
        assertEquals("CO", resolution.countryCode());
        assertEquals("CHECKOUT", resolution.operationCode());
        assertEquals(1, policy.pullDomainEvents().size());
    }

    @Test
    void nonActivePolicyCannotBeAppliedToOperation() {
        CountryPolicy policy = CountryPolicy.rehydrate(
                "policy-1",
                OrganizationId.of("org-1"),
                CountryCode.of("CO"),
                PolicyVersion.of(1),
                "COP",
                WeekStartsOn.MONDAY,
                "18:00",
                "America/Bogota",
                30,
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                CountryPolicyStatus.SUPERSEDED,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"));

        DomainInvariantViolationException exception = assertThrows(
                DomainInvariantViolationException.class,
                () -> policy.applyToOperation("CHECKOUT", "actor-1", Instant.parse("2026-02-01T00:00:00Z")));

        assertTrue(exception.getMessage().contains("active"));
    }
}
