package com.arka.directory.application.usecase;

import com.arka.directory.application.command.ConfigureRegionalPolicyCommand;
import com.arka.directory.application.command.HandleIamUserBlockedCommand;
import com.arka.directory.application.command.UpsertOrganizationContactCommand;
import com.arka.directory.application.exception.DirectoryConflictException;
import com.arka.directory.application.mapper.result.DirectoryResultMapper;
import com.arka.directory.application.port.out.audit.DirectoryAuditPort;
import com.arka.directory.application.port.out.cache.DirectoryPolicyCachePort;
import com.arka.directory.application.port.out.external.ActorLegitimacyPort;
import com.arka.directory.application.port.out.external.ClockPort;
import com.arka.directory.application.port.out.external.GeoValidationPort;
import com.arka.directory.application.port.out.external.TaxValidationPort;
import com.arka.directory.application.port.out.persistence.DirectoryAddressPersistencePort;
import com.arka.directory.application.port.out.persistence.DirectoryContactPersistencePort;
import com.arka.directory.application.port.out.persistence.DirectoryCountryPolicyPersistencePort;
import com.arka.directory.application.port.out.persistence.DirectoryLegalProfilePersistencePort;
import com.arka.directory.application.port.out.persistence.DirectoryOrganizationPersistencePort;
import com.arka.directory.application.port.out.persistence.DirectoryUserProfilePersistencePort;
import com.arka.directory.application.port.out.persistence.OutboxPersistencePort;
import com.arka.directory.application.query.ResolveCheckoutAddressQuery;
import com.arka.directory.application.result.CountryPolicyResult;
import com.arka.directory.application.result.OrganizationUserProfileResult;
import com.arka.directory.application.service.DirectoryApplicationService;
import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.domain.countrypolicy.enumtype.WeekStartsOn;
import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.entity.Address;
import com.arka.directory.domain.organizationcontext.entity.OrganizationUserProfile;
import com.arka.directory.domain.organizationcontext.enumtype.AddressStatus;
import com.arka.directory.domain.organizationcontext.enumtype.AddressType;
import com.arka.directory.domain.organizationcontext.enumtype.AddressValidationStatus;
import com.arka.directory.domain.organizationcontext.enumtype.ContactType;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationStatus;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationUserProfileStatus;
import com.arka.directory.domain.organizationcontext.service.OrganizationContextService;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectoryApplicationServiceTest {

    @Mock
    private DirectoryOrganizationPersistencePort organizationPersistencePort;
    @Mock
    private DirectoryLegalProfilePersistencePort legalProfilePersistencePort;
    @Mock
    private DirectoryUserProfilePersistencePort userProfilePersistencePort;
    @Mock
    private DirectoryContactPersistencePort contactPersistencePort;
    @Mock
    private DirectoryAddressPersistencePort addressPersistencePort;
    @Mock
    private DirectoryCountryPolicyPersistencePort countryPolicyPersistencePort;
    @Mock
    private DirectoryAuditPort directoryAuditPort;
    @Mock
    private DirectoryPolicyCachePort directoryPolicyCachePort;
    @Mock
    private ActorLegitimacyPort actorLegitimacyPort;
    @Mock
    private TaxValidationPort taxValidationPort;
    @Mock
    private GeoValidationPort geoValidationPort;
    @Mock
    private OutboxPersistencePort outboxPersistencePort;
    @Mock
    private ClockPort clockPort;

    private DirectoryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new DirectoryApplicationService(
                organizationPersistencePort,
                legalProfilePersistencePort,
                userProfilePersistencePort,
                contactPersistencePort,
                addressPersistencePort,
                countryPolicyPersistencePort,
                directoryAuditPort,
                directoryPolicyCachePort,
                actorLegitimacyPort,
                taxValidationPort,
                geoValidationPort,
                outboxPersistencePort,
                clockPort,
                new OrganizationContextService(),
                new DirectoryResultMapper());
    }

    @Test
    void upsertOrganizationContactRejectsDuplicateActiveValue() {
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(actorLegitimacyPort.isLegitimate("actor-1")).thenReturn(Mono.just(true));
        when(organizationPersistencePort.findById("org-1")).thenReturn(Mono.just(activeOrganization()));
        when(contactPersistencePort.existsActiveValue("org-1", ContactType.EMAIL, "CONTACT@ACME.COM", "contact-1"))
                .thenReturn(Mono.just(true));
        when(contactPersistencePort.clearPrimaryForType(anyString(), any())).thenReturn(Mono.empty());
        when(contactPersistencePort.findById(anyString(), anyString())).thenReturn(Mono.empty());

        UpsertOrganizationContactCommand command = new UpsertOrganizationContactCommand(
                "org-1",
                "contact-1",
                "EMAIL",
                "Institucional",
                "contact@acme.com",
                "c***@acme.com",
                true,
                "ACTIVE",
                "actor-1",
                "org-1");

        StepVerifier.create(service.handle(command))
                .expectError(DirectoryConflictException.class)
                .verify();

        verify(contactPersistencePort, never()).upsert(any());
    }

    @Test
    void handleIamUserBlockedDeactivatesOrganizationUserProfile() {
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(actorLegitimacyPort.isLegitimate("actor-1")).thenReturn(Mono.just(true));
        when(organizationPersistencePort.findById("org-1")).thenReturn(Mono.just(activeOrganization()));
        when(userProfilePersistencePort.findByOrganizationAndIamUserId("org-1", "iam-1"))
                .thenReturn(Mono.just(activeUserProfile()));
        when(userProfilePersistencePort.upsert(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(directoryAuditPort.record(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(outboxPersistencePort.storeAll(anyDomainEvents())).thenReturn(Mono.empty());

        HandleIamUserBlockedCommand command = new HandleIamUserBlockedCommand("org-1", "iam-1", "actor-1", "org-1");

        StepVerifier.create(service.handle(command))
                .assertNext(result -> {
                    OrganizationUserProfileResult profile = result;
                    org.junit.jupiter.api.Assertions.assertEquals("INACTIVE", profile.status());
                })
                .verifyComplete();

        verify(userProfilePersistencePort).upsert(any());
    }

    @Test
    void configureRegionalPolicySupersedesCurrentAndStoresOutbox() {
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(actorLegitimacyPort.isLegitimate("actor-1")).thenReturn(Mono.just(true));
        when(organizationPersistencePort.findById("org-1")).thenReturn(Mono.just(activeOrganization()));
        when(countryPolicyPersistencePort.findLatestByOrganizationAndCountry(anyString(), anyString()))
                .thenReturn(Mono.just(activePolicy(true)));
        when(countryPolicyPersistencePort.supersedeActiveByOrganizationAndCountry("org-1", "CO"))
                .thenReturn(Mono.empty());
        when(countryPolicyPersistencePort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(directoryPolicyCachePort.evictActive("org-1", "CO")).thenReturn(Mono.empty());
        when(directoryPolicyCachePort.putActive(any())).thenReturn(Mono.empty());
        when(directoryAuditPort.record(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(outboxPersistencePort.storeAll(anyDomainEvents())).thenReturn(Mono.empty());

        ConfigureRegionalPolicyCommand command = new ConfigureRegionalPolicyCommand(
                "org-1",
                "CO",
                "COP",
                "MONDAY",
                "18:00",
                "America/Bogota",
                45,
                true,
                Instant.parse("2026-01-02T00:00:00Z"),
                "actor-1",
                "org-1");

        StepVerifier.create(service.handle(command))
                .assertNext(result -> {
                    CountryPolicyResult policy = result;
                    org.junit.jupiter.api.Assertions.assertEquals(3L, policy.policyVersion());
                    org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", policy.status());
                })
                .verifyComplete();

        verify(countryPolicyPersistencePort).supersedeActiveByOrganizationAndCountry("org-1", "CO");
        verify(outboxPersistencePort).storeAll(anyDomainEvents());
    }

    @Test
    void resolveCheckoutAddressFailsWhenPolicyRequiresVerification() {
        when(actorLegitimacyPort.isLegitimate("actor-1")).thenReturn(Mono.just(true));
        when(organizationPersistencePort.findById("org-1")).thenReturn(Mono.just(activeOrganization()));
        when(directoryPolicyCachePort.findActive("org-1", "CO")).thenReturn(Mono.empty());
        when(countryPolicyPersistencePort.findActiveByOrganizationAndCountry("org-1", "CO"))
                .thenReturn(Mono.just(activePolicy(true)));
        when(directoryPolicyCachePort.putActive(any())).thenReturn(Mono.empty());
        when(addressPersistencePort.findById("org-1", "address-1"))
                .thenReturn(Mono.just(unverifiedActiveAddress()));

        ResolveCheckoutAddressQuery query = new ResolveCheckoutAddressQuery(
                "org-1",
                "address-1",
                "CO",
                "actor-1",
                "org-1");

        StepVerifier.create(service.handle(query))
                .expectError(DomainInvariantViolationException.class)
                .verify();
    }

    private Organization activeOrganization() {
        return Organization.rehydrate(
                OrganizationId.of("org-1"),
                "Acme Corp",
                "Acme",
                CountryCode.of("CO"),
                "COP",
                "America/Bogota",
                "SMB",
                OrganizationStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private CountryPolicy activePolicy(boolean requiresVerifiedAddress) {
        return CountryPolicy.rehydrate(
                "policy-1",
                OrganizationId.of("org-1"),
                CountryCode.of("CO"),
                PolicyVersion.of(2),
                "COP",
                WeekStartsOn.MONDAY,
                "18:00",
                "America/Bogota",
                30,
                requiresVerifiedAddress,
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                CountryPolicyStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private Address unverifiedActiveAddress() {
        return new Address(
                "address-1",
                "org-1",
                AddressType.SHIPPING,
                "Main",
                "Street 123",
                null,
                "Bogota",
                "Cundinamarca",
                "110111",
                "CO",
                null,
                null,
                null,
                true,
                AddressStatus.ACTIVE,
                AddressValidationStatus.PENDING,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private OrganizationUserProfile activeUserProfile() {
        return new OrganizationUserProfile(
                "profile-1",
                "org-1",
                "iam-1",
                "User",
                "Ops",
                "Operations",
                "es-CO",
                "America/Bogota",
                "ops-role",
                "ORG",
                OrganizationUserProfileStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    @SuppressWarnings("unchecked")
    private Iterable<? extends DomainEvent> anyDomainEvents() {
        return any(Iterable.class);
    }
}
