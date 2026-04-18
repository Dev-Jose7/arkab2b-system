package com.arka.directory.domain.organizationcontext.service;

import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationStatus;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganizationDomainModelTest {

    @Test
    void registerCreatesOnboardingOrganizationAndEmitsEvent() {
        Organization organization = Organization.register(
                OrganizationId.of("org-1"),
                "ACME",
                "Acme Corp",
                "Acme",
                CountryCode.of("CO"),
                "COP",
                "America/Bogota",
                "SMB",
                Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals(OrganizationStatus.ONBOARDING, organization.status());
        List<DomainEvent> events = organization.pullDomainEvents();
        assertEquals(1, events.size());
        assertEquals("OrganizationRegistered", events.getFirst().eventType());
    }

    @Test
    void inactiveOrganizationCannotTransitionBackToActive() {
        Organization organization = Organization.rehydrate(
                OrganizationId.of("org-1"),
                "ACME",
                "Acme Corp",
                "Acme",
                CountryCode.of("CO"),
                "COP",
                "America/Bogota",
                "SMB",
                OrganizationStatus.INACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));

        assertThrows(
                DomainInvariantViolationException.class,
                () -> organization.transitionStatus(OrganizationStatus.ACTIVE, Instant.parse("2026-01-02T00:00:00Z")));
    }

    @Test
    void sensitiveOperationsRequireActiveOrganization() {
        Organization organization = Organization.rehydrate(
                OrganizationId.of("org-1"),
                "ACME",
                "Acme Corp",
                "Acme",
                CountryCode.of("CO"),
                "COP",
                "America/Bogota",
                "SMB",
                OrganizationStatus.SUSPENDED,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));

        DomainInvariantViolationException exception = assertThrows(
                DomainInvariantViolationException.class,
                organization::ensureActiveForSensitiveOperation);

        assertTrue(exception.getMessage().contains("ACTIVE"));
    }
}
