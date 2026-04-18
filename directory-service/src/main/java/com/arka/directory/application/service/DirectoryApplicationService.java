package com.arka.directory.application.service;

import com.arka.directory.application.command.ApplyRegionalPolicyToOperationCommand;
import com.arka.directory.application.command.ConfigureRegionalPolicyCommand;
import com.arka.directory.application.command.CreateOrganizationCommand;
import com.arka.directory.application.command.DeactivateAddressCommand;
import com.arka.directory.application.command.DeactivateOrganizationContactCommand;
import com.arka.directory.application.command.DeactivateOrganizationUserProfileCommand;
import com.arka.directory.application.command.HandleIamUserBlockedCommand;
import com.arka.directory.application.command.MarkDefaultAddressCommand;
import com.arka.directory.application.command.UpdateOrganizationStatusCommand;
import com.arka.directory.application.command.UpsertAddressCommand;
import com.arka.directory.application.command.UpsertOrganizationContactCommand;
import com.arka.directory.application.command.UpsertOrganizationLegalProfileCommand;
import com.arka.directory.application.command.UpsertOrganizationUserProfileCommand;
import com.arka.directory.application.exception.DirectoryConflictException;
import com.arka.directory.application.exception.DirectoryResourceNotFoundException;
import com.arka.directory.application.exception.DirectoryValidationException;
import com.arka.directory.application.mapper.result.DirectoryResultMapper;
import com.arka.directory.application.port.in.ApplyRegionalPolicyToOperationCommandUseCase;
import com.arka.directory.application.port.in.ConfigureRegionalPolicyCommandUseCase;
import com.arka.directory.application.port.in.CreateOrganizationCommandUseCase;
import com.arka.directory.application.port.in.DeactivateOrganizationAddressCommandUseCase;
import com.arka.directory.application.port.in.DeactivateOrganizationContactCommandUseCase;
import com.arka.directory.application.port.in.DeactivateOrganizationUserProfileCommandUseCase;
import com.arka.directory.application.port.in.GetActiveCountryPolicyQueryUseCase;
import com.arka.directory.application.port.in.GetDirectoryAdminSummaryQueryUseCase;
import com.arka.directory.application.port.in.GetDirectoryAuditQueryUseCase;
import com.arka.directory.application.port.in.GetOrganizationProfileQueryUseCase;
import com.arka.directory.application.port.in.GetOrganizationQueryUseCase;
import com.arka.directory.application.port.in.HandleIamUserBlockedCommandUseCase;
import com.arka.directory.application.port.in.ListOrganizationAddressesQueryUseCase;
import com.arka.directory.application.port.in.ListOrganizationContactsQueryUseCase;
import com.arka.directory.application.port.in.MarkDefaultOrganizationAddressCommandUseCase;
import com.arka.directory.application.port.in.ResolveCheckoutAddressQueryUseCase;
import com.arka.directory.application.port.in.UpdateOrganizationStatusCommandUseCase;
import com.arka.directory.application.port.in.UpsertOrganizationAddressCommandUseCase;
import com.arka.directory.application.port.in.UpsertOrganizationContactCommandUseCase;
import com.arka.directory.application.port.in.UpsertOrganizationLegalProfileCommandUseCase;
import com.arka.directory.application.port.in.UpsertOrganizationUserProfileCommandUseCase;
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
import com.arka.directory.application.query.GetActiveCountryPolicyQuery;
import com.arka.directory.application.query.GetDirectoryAdminSummaryQuery;
import com.arka.directory.application.query.GetDirectoryAuditQuery;
import com.arka.directory.application.query.GetOrganizationProfileQuery;
import com.arka.directory.application.query.GetOrganizationQuery;
import com.arka.directory.application.query.ListOrganizationAddressesQuery;
import com.arka.directory.application.query.ListOrganizationContactsQuery;
import com.arka.directory.application.query.ResolveCheckoutAddressQuery;
import com.arka.directory.application.result.AddressResult;
import com.arka.directory.application.result.CheckoutAddressResolutionResult;
import com.arka.directory.application.result.CountryPolicyResult;
import com.arka.directory.application.result.DirectoryAdminSummaryResult;
import com.arka.directory.application.result.DirectoryAuditEntryResult;
import com.arka.directory.application.result.DirectoryAuditResult;
import com.arka.directory.application.result.OrganizationContactResult;
import com.arka.directory.application.result.OrganizationLegalProfileResult;
import com.arka.directory.application.result.OrganizationProfileResult;
import com.arka.directory.application.result.OrganizationResult;
import com.arka.directory.application.result.OrganizationUserProfileResult;
import com.arka.directory.application.result.RegionalPolicyApplicationResult;
import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.entity.Address;
import com.arka.directory.domain.organizationcontext.entity.OrganizationContact;
import com.arka.directory.domain.organizationcontext.entity.OrganizationLegalProfile;
import com.arka.directory.domain.organizationcontext.entity.OrganizationUserProfile;
import com.arka.directory.domain.organizationcontext.enumtype.AddressStatus;
import com.arka.directory.domain.organizationcontext.enumtype.AddressType;
import com.arka.directory.domain.organizationcontext.enumtype.AddressValidationStatus;
import com.arka.directory.domain.organizationcontext.enumtype.ContactType;
import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationContactStatus;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationLegalProfileStatus;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationStatus;
import com.arka.directory.domain.organizationcontext.enumtype.OrganizationUserProfileStatus;
import com.arka.directory.domain.countrypolicy.enumtype.WeekStartsOn;
import com.arka.directory.domain.organizationcontext.event.DirectoryEntityMutated;
import com.arka.directory.domain.organizationcontext.service.OrganizationContextService;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationContext;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.domain.shared.exception.OperationNotPermittedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DirectoryApplicationService implements
        CreateOrganizationCommandUseCase,
        UpdateOrganizationStatusCommandUseCase,
        UpsertOrganizationLegalProfileCommandUseCase,
        UpsertOrganizationUserProfileCommandUseCase,
        DeactivateOrganizationUserProfileCommandUseCase,
        HandleIamUserBlockedCommandUseCase,
        UpsertOrganizationContactCommandUseCase,
        DeactivateOrganizationContactCommandUseCase,
        UpsertOrganizationAddressCommandUseCase,
        DeactivateOrganizationAddressCommandUseCase,
        MarkDefaultOrganizationAddressCommandUseCase,
        ConfigureRegionalPolicyCommandUseCase,
        ApplyRegionalPolicyToOperationCommandUseCase,
        GetOrganizationQueryUseCase,
        GetOrganizationProfileQueryUseCase,
        ListOrganizationContactsQueryUseCase,
        ListOrganizationAddressesQueryUseCase,
        ResolveCheckoutAddressQueryUseCase,
        GetActiveCountryPolicyQueryUseCase,
        GetDirectoryAdminSummaryQueryUseCase,
        GetDirectoryAuditQueryUseCase {

    private final DirectoryOrganizationPersistencePort organizationPersistencePort;
    private final DirectoryLegalProfilePersistencePort legalProfilePersistencePort;
    private final DirectoryUserProfilePersistencePort userProfilePersistencePort;
    private final DirectoryContactPersistencePort contactPersistencePort;
    private final DirectoryAddressPersistencePort addressPersistencePort;
    private final DirectoryCountryPolicyPersistencePort countryPolicyPersistencePort;
    private final DirectoryAuditPort directoryAuditPort;
    private final DirectoryPolicyCachePort directoryPolicyCachePort;
    private final ActorLegitimacyPort actorLegitimacyPort;
    private final TaxValidationPort taxValidationPort;
    private final GeoValidationPort geoValidationPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;
    private final OrganizationContextService organizationContextService;
    private final DirectoryResultMapper resultMapper;

    public DirectoryApplicationService(
            DirectoryOrganizationPersistencePort organizationPersistencePort,
            DirectoryLegalProfilePersistencePort legalProfilePersistencePort,
            DirectoryUserProfilePersistencePort userProfilePersistencePort,
            DirectoryContactPersistencePort contactPersistencePort,
            DirectoryAddressPersistencePort addressPersistencePort,
            DirectoryCountryPolicyPersistencePort countryPolicyPersistencePort,
            DirectoryAuditPort directoryAuditPort,
            DirectoryPolicyCachePort directoryPolicyCachePort,
            ActorLegitimacyPort actorLegitimacyPort,
            TaxValidationPort taxValidationPort,
            GeoValidationPort geoValidationPort,
            OutboxPersistencePort outboxPersistencePort,
            ClockPort clockPort,
            OrganizationContextService organizationContextService,
            DirectoryResultMapper resultMapper) {
        this.organizationPersistencePort = organizationPersistencePort;
        this.legalProfilePersistencePort = legalProfilePersistencePort;
        this.userProfilePersistencePort = userProfilePersistencePort;
        this.contactPersistencePort = contactPersistencePort;
        this.addressPersistencePort = addressPersistencePort;
        this.countryPolicyPersistencePort = countryPolicyPersistencePort;
        this.directoryAuditPort = directoryAuditPort;
        this.directoryPolicyCachePort = directoryPolicyCachePort;
        this.actorLegitimacyPort = actorLegitimacyPort;
        this.taxValidationPort = taxValidationPort;
        this.geoValidationPort = geoValidationPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
        this.organizationContextService = organizationContextService;
        this.resultMapper = resultMapper;
    }

    @Override
    public Mono<OrganizationResult> handle(CreateOrganizationCommand command) {
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        return ensureActorLegitimate(actorUserId)
                .then(organizationPersistencePort.existsByCode(normalizeRequired(command.organizationCode(), "organizationCode")))
                .flatMap(exists -> exists
                        ? Mono.error(new DirectoryConflictException("Organization code already exists"))
                        : Mono.empty())
                .then(Mono.defer(() -> {
                    Instant now = now();
                    Organization organization = Organization.register(
                            OrganizationId.of(UUID.randomUUID().toString()),
                            command.organizationCode().trim().toUpperCase(),
                            command.legalName(),
                            command.tradeName(),
                            CountryCode.of(command.countryCode()),
                            command.currencyCode(),
                            command.timezone(),
                            command.segmentTier(),
                            now);
                    return organizationPersistencePort.save(organization)
                            .flatMap(saved -> registerMutation(
                                            saved.id().value(),
                                            actorUserId,
                                            "CreateOrganization",
                                            "Organization",
                                            saved.id().value(),
                                            payload("organizationCode", saved.organizationCode()),
                                            organization.pullDomainEvents())
                                    .thenReturn(resultMapper.toResult(saved)));
                }));
    }

    @Override
    public Mono<OrganizationResult> handle(UpdateOrganizationStatusCommand command) {
        Instant now = now();
        OrganizationStatus targetStatus = parseEnum(OrganizationStatus.class, command.status(), "status");
        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> validateActivationPrerequisitesIfRequired(organization, targetStatus)
                        .then(Mono.fromRunnable(() -> organization.transitionStatus(targetStatus, now)))
                        .then(organizationPersistencePort.save(organization))
                        .flatMap(saved -> registerMutation(
                                        saved.id().value(),
                                        normalizeRequired(command.actorUserId(), "actorUserId"),
                                        "UpdateOrganizationStatus",
                                        "Organization",
                                        saved.id().value(),
                                        payload("status", saved.status().name()),
                                        organization.pullDomainEvents())
                                .thenReturn(resultMapper.toResult(saved))));
    }

    @Override
    public Mono<OrganizationLegalProfileResult> handle(UpsertOrganizationLegalProfileCommand command) {
        Instant now = now();
        OrganizationLegalProfileStatus verificationStatus = parseEnum(
                OrganizationLegalProfileStatus.class,
                command.verificationStatus() == null ? "PENDING" : command.verificationStatus(),
                "verificationStatus");

        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> {
                    organization.ensureAllowsMutations();
                    return taxValidationPort
                            .isTaxIdValid(command.countryCode(), command.taxIdType(), command.taxId())
                            .flatMap(valid -> valid
                                    ? Mono.empty()
                                    : Mono.error(new DirectoryValidationException("Tax ID is not valid for country")))
                            .then(legalProfilePersistencePort.existsActiveOrganizationWithTaxId(
                                    command.countryCode(),
                                    command.taxId(),
                                    organization.id().value()))
                            .flatMap(duplicate -> duplicate
                                    ? Mono.error(new DirectoryConflictException("Tax ID is already assigned to another active organization"))
                                    : Mono.empty())
                            .then(legalProfilePersistencePort.findByOrganizationId(organization.id().value())
                                    .map(existing -> existing.upsert(
                                            command.taxIdType(),
                                            command.taxId(),
                                            command.fiscalRegime(),
                                            command.legalRepresentative(),
                                            command.countryCode(),
                                            verificationStatus,
                                            now))
                                    .switchIfEmpty(Mono.fromSupplier(() -> new OrganizationLegalProfile(
                                            UUID.randomUUID().toString(),
                                            organization.id().value(),
                                            command.taxIdType(),
                                            command.taxId(),
                                            command.fiscalRegime(),
                                            command.legalRepresentative(),
                                            command.countryCode(),
                                            verificationStatus,
                                            verificationStatus.isVerified() ? now : null,
                                            now,
                                            now))))
                            .flatMap(legalProfilePersistencePort::upsert)
                            .flatMap(saved -> registerMutation(
                                            organization.id().value(),
                                            normalizeRequired(command.actorUserId(), "actorUserId"),
                                            "UpsertOrganizationLegalProfile",
                                            "OrganizationLegalProfile",
                                            saved.legalProfileId(),
                                            payload("verificationStatus", saved.verificationStatus().name()),
                                            List.of())
                                    .thenReturn(resultMapper.toResult(saved)));
                });
    }

    @Override
    public Mono<OrganizationUserProfileResult> handle(UpsertOrganizationUserProfileCommand command) {
        Instant now = now();
        OrganizationUserProfileStatus status = parseEnum(
                OrganizationUserProfileStatus.class,
                command.status() == null ? "ACTIVE" : command.status(),
                "status");

        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> {
                    organization.ensureAllowsMutations();
                    return userProfilePersistencePort
                            .findByOrganizationAndIamUserId(organization.id().value(), normalizeRequired(command.iamUserId(), "iamUserId"))
                            .map(existing -> new OrganizationUserProfile(
                                    existing.userProfileId(),
                                    organization.id().value(),
                                    normalizeRequired(command.iamUserId(), "iamUserId"),
                                    command.displayName(),
                                    command.jobTitle(),
                                    command.department(),
                                    command.locale(),
                                    command.timezone(),
                                    command.roleReference(),
                                    command.ownershipScope(),
                                    status,
                                    existing.createdAt(),
                                    now))
                            .switchIfEmpty(Mono.fromSupplier(() -> new OrganizationUserProfile(
                                    command.userProfileId() == null || command.userProfileId().isBlank()
                                            ? UUID.randomUUID().toString()
                                            : command.userProfileId().trim(),
                                    organization.id().value(),
                                    normalizeRequired(command.iamUserId(), "iamUserId"),
                                    command.displayName(),
                                    command.jobTitle(),
                                    command.department(),
                                    command.locale(),
                                    command.timezone(),
                                    command.roleReference(),
                                    command.ownershipScope(),
                                    status,
                                    now,
                                    now)))
                            .flatMap(userProfilePersistencePort::upsert)
                            .flatMap(saved -> registerMutation(
                                            organization.id().value(),
                                            normalizeRequired(command.actorUserId(), "actorUserId"),
                                            "UpsertOrganizationUserProfile",
                                            "OrganizationUserProfile",
                                            saved.userProfileId(),
                                            payload("iamUserId", saved.iamUserId()),
                                            List.of())
                                    .thenReturn(resultMapper.toResult(saved)));
                });
    }

    @Override
    public Mono<OrganizationUserProfileResult> handle(DeactivateOrganizationUserProfileCommand command) {
        Instant now = now();
        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> userProfilePersistencePort
                        .findByOrganizationAndIamUserId(organization.id().value(), normalizeRequired(command.iamUserId(), "iamUserId"))
                        .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Organization user profile not found")))
                        .map(existing -> existing.deactivateFromUserBlocked(now))
                        .flatMap(userProfilePersistencePort::upsert)
                        .flatMap(saved -> registerMutation(
                                        organization.id().value(),
                                        normalizeRequired(command.actorUserId(), "actorUserId"),
                                        "DeactivateOrganizationUserProfile",
                                        "OrganizationUserProfile",
                                        saved.userProfileId(),
                                        payload("reason", command.reason()),
                                        List.of())
                                .thenReturn(resultMapper.toResult(saved))));
    }

    @Override
    public Mono<OrganizationUserProfileResult> handle(HandleIamUserBlockedCommand command) {
        Instant now = now();
        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> userProfilePersistencePort
                        .findByOrganizationAndIamUserId(organization.id().value(), normalizeRequired(command.iamUserId(), "iamUserId"))
                        .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Organization user profile not found")))
                        .map(existing -> existing.deactivateFromUserBlocked(now))
                        .flatMap(userProfilePersistencePort::upsert)
                        .flatMap(saved -> registerMutation(
                                        organization.id().value(),
                                        normalizeRequired(command.actorUserId(), "actorUserId"),
                                        "HandleIamUserBlocked",
                                        "OrganizationUserProfile",
                                        saved.userProfileId(),
                                        payload("iamUserId", saved.iamUserId()),
                                        List.of())
                                .thenReturn(resultMapper.toResult(saved))));
    }

    @Override
    public Mono<OrganizationContactResult> handle(UpsertOrganizationContactCommand command) {
        Instant now = now();
        ContactType contactType = parseEnum(ContactType.class, command.contactType(), "contactType");
        OrganizationContactStatus status = parseEnum(
                OrganizationContactStatus.class,
                command.status() == null ? "ACTIVE" : command.status(),
                "status");
        boolean primary = command.primary() != null && command.primary();

        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> {
                    organization.ensureAllowsMutations();
                    String contactId = command.contactId() == null || command.contactId().isBlank()
                            ? UUID.randomUUID().toString()
                            : command.contactId().trim();

                    return contactPersistencePort
                            .existsActiveValue(
                                    organization.id().value(),
                                    contactType,
                                    normalizeRequired(command.value(), "value").trim().toUpperCase(),
                                    contactId)
                            .flatMap(duplicate -> duplicate
                                    ? Mono.error(new DirectoryConflictException("Active contact value already exists for organization/type"))
                                    : Mono.empty())
                            .then(primary && status.isActive()
                                    ? contactPersistencePort.clearPrimaryForType(organization.id().value(), contactType)
                                    : Mono.empty())
                            .then(contactPersistencePort.findById(organization.id().value(), contactId)
                                    .map(existing -> new OrganizationContact(
                                            existing.contactId(),
                                            organization.id().value(),
                                            contactType,
                                            command.label(),
                                            command.value(),
                                            command.valueMasked(),
                                            primary,
                                            status,
                                            existing.createdAt(),
                                            now))
                                    .switchIfEmpty(Mono.fromSupplier(() -> new OrganizationContact(
                                            contactId,
                                            organization.id().value(),
                                            contactType,
                                            command.label(),
                                            command.value(),
                                            command.valueMasked(),
                                            primary,
                                            status,
                                            now,
                                            now))))
                            .flatMap(contactPersistencePort::upsert)
                            .flatMap(saved -> registerMutation(
                                            organization.id().value(),
                                            normalizeRequired(command.actorUserId(), "actorUserId"),
                                            "UpsertOrganizationContact",
                                            "OrganizationContact",
                                            saved.contactId(),
                                            payload("primary", Boolean.toString(saved.primary())),
                                            List.of())
                                    .thenReturn(resultMapper.toResult(saved)));
                });
    }

    @Override
    public Mono<OrganizationContactResult> handle(DeactivateOrganizationContactCommand command) {
        Instant now = now();
        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> contactPersistencePort
                        .findById(organization.id().value(), normalizeRequired(command.contactId(), "contactId"))
                        .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Organization contact not found")))
                        .map(existing -> existing.deactivate(now))
                        .flatMap(contactPersistencePort::upsert)
                        .flatMap(saved -> registerMutation(
                                        organization.id().value(),
                                        normalizeRequired(command.actorUserId(), "actorUserId"),
                                        "DeactivateOrganizationContact",
                                        "OrganizationContact",
                                        saved.contactId(),
                                        payload("status", saved.status().name()),
                                        List.of())
                                .thenReturn(resultMapper.toResult(saved))));
    }

    @Override
    public Mono<AddressResult> handle(UpsertAddressCommand command) {
        Instant now = now();
        AddressType addressType = parseEnum(AddressType.class, command.addressType(), "addressType");
        AddressStatus status = parseEnum(
                AddressStatus.class,
                command.status() == null ? "ACTIVE" : command.status(),
                "status");
        AddressValidationStatus validationStatus = parseEnum(
                AddressValidationStatus.class,
                command.validationStatus() == null ? "PENDING" : command.validationStatus(),
                "validationStatus");
        boolean isDefault = command.isDefault() != null && command.isDefault();

        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> {
                    organization.ensureAllowsMutations();
                    return geoValidationPort
                            .isAddressValid(command.countryCode(), command.city(), command.postalCode(), command.line1())
                            .flatMap(valid -> valid
                                    ? Mono.empty()
                                    : Mono.error(new DirectoryValidationException("Address validation failed")))
                            .then(Mono.defer(() -> {
                                String addressId = command.addressId() == null || command.addressId().isBlank()
                                        ? UUID.randomUUID().toString()
                                        : command.addressId().trim();
                                Mono<Void> clearDefault = isDefault && status.isActive()
                                        ? addressPersistencePort.clearDefaultForType(organization.id().value(), addressType)
                                        : Mono.empty();

                                Mono<Address> addressCandidate = addressPersistencePort
                                        .findById(organization.id().value(), addressId)
                                        .map(existing -> new Address(
                                                existing.addressId(),
                                                organization.id().value(),
                                                addressType,
                                                command.alias(),
                                                command.line1(),
                                                command.line2(),
                                                command.city(),
                                                command.stateRegion(),
                                                command.postalCode(),
                                                command.countryCode(),
                                                command.reference(),
                                                command.latitude(),
                                                command.longitude(),
                                                isDefault,
                                                status,
                                                validationStatus,
                                                validationStatus.isVerified() ? now : existing.validatedAt(),
                                                existing.createdAt(),
                                                now))
                                        .switchIfEmpty(Mono.fromSupplier(() -> new Address(
                                                addressId,
                                                organization.id().value(),
                                                addressType,
                                                command.alias(),
                                                command.line1(),
                                                command.line2(),
                                                command.city(),
                                                command.stateRegion(),
                                                command.postalCode(),
                                                command.countryCode(),
                                                command.reference(),
                                                command.latitude(),
                                                command.longitude(),
                                                isDefault,
                                                status,
                                                validationStatus,
                                                validationStatus.isVerified() ? now : null,
                                                now,
                                                now)));

                                return clearDefault
                                        .then(addressCandidate)
                                        .flatMap(addressPersistencePort::upsert)
                                        .flatMap(saved -> registerMutation(
                                                        organization.id().value(),
                                                        normalizeRequired(command.actorUserId(), "actorUserId"),
                                                        "UpsertAddress",
                                                        "Address",
                                                        saved.addressId(),
                                                        payload("isDefault", Boolean.toString(saved.isDefault())),
                                                        List.of())
                                                .thenReturn(resultMapper.toResult(saved)));
                            }));
                });
    }

    @Override
    public Mono<AddressResult> handle(DeactivateAddressCommand command) {
        Instant now = now();
        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> addressPersistencePort
                        .findById(organization.id().value(), normalizeRequired(command.addressId(), "addressId"))
                        .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Address not found")))
                        .map(existing -> existing.deactivate(now))
                        .flatMap(addressPersistencePort::upsert)
                        .flatMap(saved -> registerMutation(
                                        organization.id().value(),
                                        normalizeRequired(command.actorUserId(), "actorUserId"),
                                        "DeactivateAddress",
                                        "Address",
                                        saved.addressId(),
                                        payload("status", saved.status().name()),
                                        List.of())
                                .thenReturn(resultMapper.toResult(saved))));
    }

    @Override
    public Mono<AddressResult> handle(MarkDefaultAddressCommand command) {
        Instant now = now();
        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> addressPersistencePort
                        .findById(organization.id().value(), normalizeRequired(command.addressId(), "addressId"))
                        .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Address not found")))
                        .flatMap(existing -> {
                            AddressType addressType = existing.addressType();
                            if (command.addressType() != null && !command.addressType().isBlank()) {
                                addressType = parseEnum(AddressType.class, command.addressType(), "addressType");
                            }
                            AddressType finalAddressType = addressType;
                            return addressPersistencePort
                                    .clearDefaultForType(organization.id().value(), finalAddressType)
                                    .then(Mono.fromSupplier(() -> existing.markDefault(true, now)))
                                    .flatMap(addressPersistencePort::upsert);
                        })
                        .flatMap(saved -> registerMutation(
                                        organization.id().value(),
                                        normalizeRequired(command.actorUserId(), "actorUserId"),
                                        "MarkDefaultAddress",
                                        "Address",
                                        saved.addressId(),
                                        payload("addressType", saved.addressType().name()),
                                        List.of())
                                .thenReturn(resultMapper.toResult(saved))));
    }

    @Override
    public Mono<CountryPolicyResult> handle(ConfigureRegionalPolicyCommand command) {
        Instant now = now();
        WeekStartsOn weekStartsOn = parseEnum(WeekStartsOn.class, command.weekStartsOn(), "weekStartsOn");
        return loadOrganizationWithOwnership(command.organizationId(), command.actorUserId(), command.actorOrganizationId())
                .flatMap(organization -> {
                    organization.ensureAllowsMutations();
                    String countryCode = normalizeRequired(command.countryCode(), "countryCode");
                    return countryPolicyPersistencePort
                            .findLatestByOrganizationAndCountry(organization.id().value(), countryCode)
                            .map(CountryPolicy::policyVersion)
                            .flatMap(previousVersion -> persistConfiguredPolicy(
                                    organization,
                                    command,
                                    countryCode,
                                    weekStartsOn,
                                    previousVersion,
                                    now))
                            .switchIfEmpty(Mono.defer(() -> persistConfiguredPolicy(
                                    organization,
                                    command,
                                    countryCode,
                                    weekStartsOn,
                                    null,
                                    now)));
                });
    }

    @Override
    public Mono<RegionalPolicyApplicationResult> handle(ApplyRegionalPolicyToOperationCommand command) {
        Instant now = now();
        String actorUserId = normalizeRequired(command.actorUserId(), "actorUserId");
        return ensureActorLegitimate(actorUserId)
                .flatMap(legitimate -> loadOrganization(command.organizationId())
                        .flatMap(organization -> resolveActiveCountryPolicy(organization.id().value(), command.countryCode())
                                .flatMap(activePolicy -> {
                                    OrganizationContext context = new OrganizationContext(
                                            OrganizationId.of(command.organizationId()),
                                            actorUserId,
                                            command.actorOrganizationId(),
                                            legitimate,
                                            false);
                                    organizationContextService.validateSensitiveOperationContext(organization, context);
                                    var resolution = activePolicy.applyToOperation(command.operationCode(), actorUserId, now);
                                    return registerMutation(
                                                    organization.id().value(),
                                                    actorUserId,
                                                    "ApplyRegionalPolicyToOperation",
                                                    "CountryPolicy",
                                                    activePolicy.policyId(),
                                                    payload("operationCode", resolution.operationCode()),
                                                    activePolicy.pullDomainEvents())
                                            .thenReturn(new RegionalPolicyApplicationResult(
                                                    resolution.organizationId(),
                                                    resolution.countryCode(),
                                                    resolution.policyVersion(),
                                                    resolution.operationCode(),
                                                    resolution.currencyCode(),
                                                    resolution.timezone(),
                                                    resolution.weeklyCutoffLocalTime(),
                                                    resolution.reportingRetentionDays(),
                                                    "APPLIED"));
                                })));
    }

    @Override
    public Mono<OrganizationResult> handle(GetOrganizationQuery query) {
        return loadOrganizationWithOwnership(query.organizationId(), query.actorUserId(), query.actorOrganizationId())
                .map(resultMapper::toResult);
    }

    @Override
    public Mono<OrganizationProfileResult> handle(GetOrganizationProfileQuery query) {
        return loadOrganizationWithOwnership(query.organizationId(), query.actorUserId(), query.actorOrganizationId())
                .flatMap(organization -> {
                    Mono<OrganizationLegalProfileResult> legalProfile = legalProfilePersistencePort
                            .findByOrganizationId(organization.id().value())
                            .map(resultMapper::toResult)
                            .defaultIfEmpty(null);
                    Mono<List<OrganizationUserProfileResult>> userProfiles = userProfilePersistencePort
                            .findByOrganizationId(organization.id().value())
                            .map(resultMapper::toResult)
                            .collectList();
                    Mono<List<OrganizationContactResult>> contacts = contactPersistencePort
                            .findByOrganizationId(organization.id().value())
                            .map(resultMapper::toResult)
                            .collectList();
                    Mono<List<AddressResult>> addresses = addressPersistencePort
                            .findByOrganizationId(organization.id().value())
                            .map(resultMapper::toResult)
                            .collectList();
                    Mono<List<CountryPolicyResult>> activePolicies = countryPolicyPersistencePort
                            .findActiveByOrganization(organization.id().value())
                            .map(resultMapper::toResult)
                            .collectList();
                    return Mono.zip(legalProfile, userProfiles, contacts, addresses, activePolicies)
                            .map(tuple -> new OrganizationProfileResult(
                                    resultMapper.toResult(organization),
                                    tuple.getT1(),
                                    tuple.getT2(),
                                    tuple.getT3(),
                                    tuple.getT4(),
                                    tuple.getT5()));
                });
    }

    @Override
    public Flux<OrganizationContactResult> handle(ListOrganizationContactsQuery query) {
        return loadOrganizationWithOwnership(query.organizationId(), query.actorUserId(), query.actorOrganizationId())
                .flatMapMany(organization -> contactPersistencePort.findByOrganizationId(organization.id().value())
                        .map(resultMapper::toResult));
    }

    @Override
    public Flux<AddressResult> handle(ListOrganizationAddressesQuery query) {
        return loadOrganizationWithOwnership(query.organizationId(), query.actorUserId(), query.actorOrganizationId())
                .flatMapMany(organization -> addressPersistencePort.findByOrganizationId(organization.id().value())
                        .map(resultMapper::toResult));
    }

    @Override
    public Mono<CheckoutAddressResolutionResult> handle(ResolveCheckoutAddressQuery query) {
        return loadOrganizationWithOwnership(query.organizationId(), query.actorUserId(), query.actorOrganizationId())
                .flatMap(organization -> resolveActiveCountryPolicy(organization.id().value(), query.countryCode())
                        .flatMap(activePolicy -> addressPersistencePort
                                .findById(organization.id().value(), query.addressId())
                                .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Address not found")))
                                .map(address -> {
                                    organizationContextService.validateCheckoutAddress(organization, activePolicy, address);
                                    return new CheckoutAddressResolutionResult(
                                            resultMapper.toResult(address),
                                            resultMapper.toResult(activePolicy),
                                            "RESOLVED");
                                })));
    }

    @Override
    public Mono<CountryPolicyResult> handle(GetActiveCountryPolicyQuery query) {
        return loadOrganizationWithOwnership(query.organizationId(), query.actorUserId(), query.actorOrganizationId())
                .flatMap(organization -> resolveActiveCountryPolicy(organization.id().value(), query.countryCode()))
                .map(resultMapper::toResult);
    }

    @Override
    public Mono<DirectoryAdminSummaryResult> handle(GetDirectoryAdminSummaryQuery query) {
        return ensureActorLegitimate(query.actorUserId())
                .then(Mono.zip(
                        organizationPersistencePort.countAll(),
                        organizationPersistencePort.countByStatus(OrganizationStatus.ACTIVE.name()),
                        organizationPersistencePort.countByStatus(OrganizationStatus.SUSPENDED.name()),
                        organizationPersistencePort.countByStatus(OrganizationStatus.INACTIVE.name()),
                        countryPolicyPersistencePort.countActive(),
                        contactPersistencePort.countActive(),
                        addressPersistencePort.countActive()))
                .map(tuple -> new DirectoryAdminSummaryResult(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        tuple.getT5(),
                        tuple.getT6(),
                        tuple.getT7()));
    }

    @Override
    public Mono<DirectoryAuditResult> handle(GetDirectoryAuditQuery query) {
        int limit = query.limit() <= 0 ? 100 : query.limit();
        return loadOrganizationWithOwnership(query.organizationId(), query.actorUserId(), query.actorOrganizationId())
                .flatMap(organization -> directoryAuditPort
                        .findByOrganization(organization.id().value(), limit)
                        .collectList()
                        .map(entries -> new DirectoryAuditResult(organization.id().value(), entries)));
    }

    private Mono<Organization> loadOrganizationWithOwnership(
            String organizationId,
            String actorUserId,
            String actorOrganizationId) {
        String normalizedOrganizationId = normalizeRequired(organizationId, "organizationId");
        String normalizedActorUserId = normalizeRequired(actorUserId, "actorUserId");

        return ensureActorLegitimate(normalizedActorUserId)
                .then(loadOrganization(normalizedOrganizationId))
                .flatMap(organization -> {
                    if (actorOrganizationId != null && !actorOrganizationId.isBlank()
                            && !organization.id().value().equals(actorOrganizationId.trim())) {
                        return Mono.error(new OperationNotPermittedException("Actor organization does not match requested organization"));
                    }
                    return Mono.just(organization);
                });
    }

    private Mono<Organization> loadOrganization(String organizationId) {
        return organizationPersistencePort
                .findById(normalizeRequired(organizationId, "organizationId"))
                .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Organization not found")));
    }

    private Mono<CountryPolicy> resolveActiveCountryPolicy(String organizationId, String countryCode) {
        String normalizedOrganizationId = normalizeRequired(organizationId, "organizationId");
        String normalizedCountryCode = normalizeRequired(countryCode, "countryCode").toUpperCase();

        return directoryPolicyCachePort
                .findActive(normalizedOrganizationId, normalizedCountryCode)
                .switchIfEmpty(countryPolicyPersistencePort
                        .findActiveByOrganizationAndCountry(normalizedOrganizationId, normalizedCountryCode)
                        .switchIfEmpty(Mono.error(new DirectoryResourceNotFoundException("Active country policy not found")))
                        .flatMap(policy -> directoryPolicyCachePort.putActive(policy).thenReturn(policy)));
    }

    private Mono<Void> validateActivationPrerequisitesIfRequired(Organization organization, OrganizationStatus targetStatus) {
        if (targetStatus != OrganizationStatus.ACTIVE) {
            return Mono.empty();
        }

        Mono<Boolean> legalVerified = legalProfilePersistencePort
                .findByOrganizationId(organization.id().value())
                .map(profile -> profile.verificationStatus().isVerified())
                .defaultIfEmpty(false);

        Mono<Boolean> activePolicyAvailable = countryPolicyPersistencePort
                .findActiveByOrganizationAndCountry(organization.id().value(), organization.countryCode().value())
                .hasElement();

        Mono<Boolean> activeContactAvailable = contactPersistencePort
                .findByOrganizationId(organization.id().value())
                .filter(contact -> contact.status().isActive())
                .hasElements();

        return Mono.zip(legalVerified, activePolicyAvailable, activeContactAvailable)
                .flatMap(tuple -> {
                    if (!tuple.getT1()) {
                        return Mono.error(new DirectoryValidationException("Cannot activate organization without verified legal profile"));
                    }
                    if (!tuple.getT2()) {
                        return Mono.error(new DirectoryValidationException("Cannot activate organization without active country policy"));
                    }
                    if (!tuple.getT3()) {
                        return Mono.error(new DirectoryValidationException("Cannot activate organization without active institutional contact"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> registerMutation(
            String organizationId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String payload,
            Iterable<? extends DomainEvent> domainEvents) {
        List<DomainEvent> events = new ArrayList<>();
        if (domainEvents != null) {
            for (DomainEvent event : domainEvents) {
                events.add(event);
            }
        }
        if (events.isEmpty()) {
            events.add(new DirectoryEntityMutated(
                    now(),
                    organizationId,
                    actionType,
                    targetType,
                    targetId,
                    actorUserId));
        }

        return directoryAuditPort
                .record(organizationId, actorUserId, actionType, targetType, targetId, "SUCCESS", payload)
                .then(outboxPersistencePort.storeAll(events));
    }

    private Mono<Boolean> ensureActorLegitimate(String actorUserId) {
        String normalizedActorUserId = normalizeRequired(actorUserId, "actorUserId");
        return actorLegitimacyPort
                .isLegitimate(normalizedActorUserId)
                .flatMap(legitimate -> legitimate
                        ? Mono.just(true)
                        : Mono.error(new OperationNotPermittedException("Actor legitimacy could not be validated")));
    }

    private Instant now() {
        return clockPort.now();
    }

    private Mono<CountryPolicyResult> persistConfiguredPolicy(
            Organization organization,
            ConfigureRegionalPolicyCommand command,
            String countryCode,
            WeekStartsOn weekStartsOn,
            PolicyVersion previousVersion,
            Instant now) {
        return Mono.defer(() -> countryPolicyPersistencePort
                .supersedeActiveByOrganizationAndCountry(organization.id().value(), countryCode)
                .then(Mono.defer(() -> {
                    CountryPolicy policy = CountryPolicy.configure(
                            UUID.randomUUID().toString(),
                            organization.id(),
                            CountryCode.of(countryCode),
                            previousVersion,
                            command.currencyCode(),
                            weekStartsOn,
                            command.weeklyCutoffLocalTime(),
                            command.timezone(),
                            command.reportingRetentionDays() == null
                                    ? 30
                                    : command.reportingRetentionDays(),
                            command.requiresVerifiedAddress() == null
                                    ? true
                                    : command.requiresVerifiedAddress(),
                            command.effectiveFrom(),
                            now);
                    return countryPolicyPersistencePort
                            .save(policy)
                            .flatMap(saved -> directoryPolicyCachePort
                                    .evictActive(saved.organizationId().value(), saved.countryCode().value())
                                    .then(directoryPolicyCachePort.putActive(saved))
                                    .then(registerMutation(
                                            organization.id().value(),
                                            normalizeRequired(command.actorUserId(), "actorUserId"),
                                            "ConfigureRegionalPolicy",
                                            "CountryPolicy",
                                            saved.policyId(),
                                            payload("policyVersion", Long.toString(saved.policyVersion().value())),
                                            policy.pullDomainEvents()))
                                    .thenReturn(resultMapper.toResult(saved)));
                })));
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DirectoryValidationException(fieldName + " is required");
        }
        return value.trim();
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        String normalized = normalizeRequired(value, fieldName).toUpperCase();
        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException exception) {
            throw new DirectoryValidationException(fieldName + " has unsupported value: " + value);
        }
    }

    private String payload(String key, String value) {
        String safeKey = key == null ? "detail" : key.trim();
        String safeValue = value == null ? "" : value.replace("\"", "");
        return "{\"" + safeKey + "\":\"" + safeValue + "\"}";
    }
}
