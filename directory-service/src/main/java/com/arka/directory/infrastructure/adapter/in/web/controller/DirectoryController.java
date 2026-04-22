package com.arka.directory.infrastructure.adapter.in.web.controller;

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
import com.arka.directory.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.directory.infrastructure.adapter.in.web.mapper.command.DirectoryCommandMapper;
import com.arka.directory.infrastructure.adapter.in.web.mapper.query.DirectoryQueryMapper;
import com.arka.directory.infrastructure.adapter.in.web.mapper.response.DirectoryResponseMapper;
import com.arka.directory.infrastructure.adapter.in.web.request.ApplyRegionalPolicyRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.ConfigureRegionalPolicyRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.CreateOrganizationRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.DeactivateOrganizationUserProfileRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpdateOrganizationStatusRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertAddressRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertOrganizationContactRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertOrganizationLegalProfileRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertOrganizationUserProfileRequest;
import com.arka.directory.infrastructure.adapter.in.web.response.AddressResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.CheckoutAddressResolutionResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.CountryPolicyResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.DirectoryAdminSummaryResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.DirectoryAuditResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationContactResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationLegalProfileResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationProfileResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationRegionalContextResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationUserProfileResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.RegionalPolicyApplicationResponse;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class DirectoryController {

    private final DirectoryCommandMapper commandMapper;
    private final DirectoryQueryMapper queryMapper;
    private final DirectoryResponseMapper responseMapper;
    private final CreateOrganizationCommandUseCase createOrganizationCommandUseCase;
    private final UpdateOrganizationStatusCommandUseCase updateOrganizationStatusCommandUseCase;
    private final UpsertOrganizationLegalProfileCommandUseCase upsertOrganizationLegalProfileCommandUseCase;
    private final UpsertOrganizationUserProfileCommandUseCase upsertOrganizationUserProfileCommandUseCase;
    private final DeactivateOrganizationUserProfileCommandUseCase deactivateOrganizationUserProfileCommandUseCase;
    private final HandleIamUserBlockedCommandUseCase handleIamUserBlockedCommandUseCase;
    private final UpsertOrganizationContactCommandUseCase upsertOrganizationContactCommandUseCase;
    private final DeactivateOrganizationContactCommandUseCase deactivateOrganizationContactCommandUseCase;
    private final UpsertOrganizationAddressCommandUseCase upsertOrganizationAddressCommandUseCase;
    private final DeactivateOrganizationAddressCommandUseCase deactivateOrganizationAddressCommandUseCase;
    private final MarkDefaultOrganizationAddressCommandUseCase markDefaultOrganizationAddressCommandUseCase;
    private final ConfigureRegionalPolicyCommandUseCase configureRegionalPolicyCommandUseCase;
    private final ApplyRegionalPolicyToOperationCommandUseCase applyRegionalPolicyToOperationCommandUseCase;
    private final GetOrganizationQueryUseCase getOrganizationQueryUseCase;
    private final GetOrganizationProfileQueryUseCase getOrganizationProfileQueryUseCase;
    private final ListOrganizationContactsQueryUseCase listOrganizationContactsQueryUseCase;
    private final ListOrganizationAddressesQueryUseCase listOrganizationAddressesQueryUseCase;
    private final ResolveCheckoutAddressQueryUseCase resolveCheckoutAddressQueryUseCase;
    private final GetActiveCountryPolicyQueryUseCase getActiveCountryPolicyQueryUseCase;
    private final GetDirectoryAdminSummaryQueryUseCase getDirectoryAdminSummaryQueryUseCase;
    private final GetDirectoryAuditQueryUseCase getDirectoryAuditQueryUseCase;

    public DirectoryController(
            DirectoryCommandMapper commandMapper,
            DirectoryQueryMapper queryMapper,
            DirectoryResponseMapper responseMapper,
            CreateOrganizationCommandUseCase createOrganizationCommandUseCase,
            UpdateOrganizationStatusCommandUseCase updateOrganizationStatusCommandUseCase,
            UpsertOrganizationLegalProfileCommandUseCase upsertOrganizationLegalProfileCommandUseCase,
            UpsertOrganizationUserProfileCommandUseCase upsertOrganizationUserProfileCommandUseCase,
            DeactivateOrganizationUserProfileCommandUseCase deactivateOrganizationUserProfileCommandUseCase,
            HandleIamUserBlockedCommandUseCase handleIamUserBlockedCommandUseCase,
            UpsertOrganizationContactCommandUseCase upsertOrganizationContactCommandUseCase,
            DeactivateOrganizationContactCommandUseCase deactivateOrganizationContactCommandUseCase,
            UpsertOrganizationAddressCommandUseCase upsertOrganizationAddressCommandUseCase,
            DeactivateOrganizationAddressCommandUseCase deactivateOrganizationAddressCommandUseCase,
            MarkDefaultOrganizationAddressCommandUseCase markDefaultOrganizationAddressCommandUseCase,
            ConfigureRegionalPolicyCommandUseCase configureRegionalPolicyCommandUseCase,
            ApplyRegionalPolicyToOperationCommandUseCase applyRegionalPolicyToOperationCommandUseCase,
            GetOrganizationQueryUseCase getOrganizationQueryUseCase,
            GetOrganizationProfileQueryUseCase getOrganizationProfileQueryUseCase,
            ListOrganizationContactsQueryUseCase listOrganizationContactsQueryUseCase,
            ListOrganizationAddressesQueryUseCase listOrganizationAddressesQueryUseCase,
            ResolveCheckoutAddressQueryUseCase resolveCheckoutAddressQueryUseCase,
            GetActiveCountryPolicyQueryUseCase getActiveCountryPolicyQueryUseCase,
            GetDirectoryAdminSummaryQueryUseCase getDirectoryAdminSummaryQueryUseCase,
            GetDirectoryAuditQueryUseCase getDirectoryAuditQueryUseCase) {
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.responseMapper = responseMapper;
        this.createOrganizationCommandUseCase = createOrganizationCommandUseCase;
        this.updateOrganizationStatusCommandUseCase = updateOrganizationStatusCommandUseCase;
        this.upsertOrganizationLegalProfileCommandUseCase = upsertOrganizationLegalProfileCommandUseCase;
        this.upsertOrganizationUserProfileCommandUseCase = upsertOrganizationUserProfileCommandUseCase;
        this.deactivateOrganizationUserProfileCommandUseCase = deactivateOrganizationUserProfileCommandUseCase;
        this.handleIamUserBlockedCommandUseCase = handleIamUserBlockedCommandUseCase;
        this.upsertOrganizationContactCommandUseCase = upsertOrganizationContactCommandUseCase;
        this.deactivateOrganizationContactCommandUseCase = deactivateOrganizationContactCommandUseCase;
        this.upsertOrganizationAddressCommandUseCase = upsertOrganizationAddressCommandUseCase;
        this.deactivateOrganizationAddressCommandUseCase = deactivateOrganizationAddressCommandUseCase;
        this.markDefaultOrganizationAddressCommandUseCase = markDefaultOrganizationAddressCommandUseCase;
        this.configureRegionalPolicyCommandUseCase = configureRegionalPolicyCommandUseCase;
        this.applyRegionalPolicyToOperationCommandUseCase = applyRegionalPolicyToOperationCommandUseCase;
        this.getOrganizationQueryUseCase = getOrganizationQueryUseCase;
        this.getOrganizationProfileQueryUseCase = getOrganizationProfileQueryUseCase;
        this.listOrganizationContactsQueryUseCase = listOrganizationContactsQueryUseCase;
        this.listOrganizationAddressesQueryUseCase = listOrganizationAddressesQueryUseCase;
        this.resolveCheckoutAddressQueryUseCase = resolveCheckoutAddressQueryUseCase;
        this.getActiveCountryPolicyQueryUseCase = getActiveCountryPolicyQueryUseCase;
        this.getDirectoryAdminSummaryQueryUseCase = getDirectoryAdminSummaryQueryUseCase;
        this.getDirectoryAuditQueryUseCase = getDirectoryAuditQueryUseCase;
    }

    @PreAuthorize("hasAuthority('directory.organization.update')")
    @PostMapping("/organizations")
    public Mono<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return createOrganizationCommandUseCase
                .handle(commandMapper.toCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.update')")
    @PostMapping("/organizations/{organizationId}/status")
    public Mono<OrganizationResponse> updateOrganizationStatus(
            @PathVariable String organizationId,
            @Valid @RequestBody UpdateOrganizationStatusRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateOrganizationStatusCommandUseCase
                .handle(commandMapper.toCommand(organizationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('directory.profile.create', 'directory.profile.update')")
    @PutMapping("/organizations/{organizationId}/legal-profile")
    public Mono<OrganizationLegalProfileResponse> upsertLegalProfile(
            @PathVariable String organizationId,
            @Valid @RequestBody UpsertOrganizationLegalProfileRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return upsertOrganizationLegalProfileCommandUseCase
                .handle(commandMapper.toCommand(organizationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('directory.profile.create', 'directory.profile.update')")
    @PutMapping("/organizations/{organizationId}/user-profiles/{iamUserId}")
    public Mono<OrganizationUserProfileResponse> upsertOrganizationUserProfile(
            @PathVariable String organizationId,
            @PathVariable String iamUserId,
            @Valid @RequestBody UpsertOrganizationUserProfileRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return upsertOrganizationUserProfileCommandUseCase
                .handle(commandMapper.toCommand(organizationId, iamUserId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('directory.profile.update')")
    @PostMapping("/organizations/{organizationId}/user-profiles/{iamUserId}/deactivate")
    public Mono<OrganizationUserProfileResponse> deactivateOrganizationUserProfile(
            @PathVariable String organizationId,
            @PathVariable String iamUserId,
            @RequestBody(required = false) DeactivateOrganizationUserProfileRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return deactivateOrganizationUserProfileCommandUseCase
                .handle(commandMapper.toCommand(organizationId, iamUserId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.profile.update')")
    @PostMapping("/organizations/{organizationId}/user-profiles/{iamUserId}/blocked")
    public Mono<OrganizationUserProfileResponse> handleIamUserBlocked(
            @PathVariable String organizationId,
            @PathVariable String iamUserId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return handleIamUserBlockedCommandUseCase
                .handle(commandMapper.toHandleIamUserBlockedCommand(organizationId, iamUserId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('directory.profile.create', 'directory.profile.update')")
    @PutMapping("/organizations/{organizationId}/contacts/{contactId}")
    public Mono<OrganizationContactResponse> upsertContact(
            @PathVariable String organizationId,
            @PathVariable String contactId,
            @Valid @RequestBody UpsertOrganizationContactRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return upsertOrganizationContactCommandUseCase
                .handle(commandMapper.toCommand(organizationId, contactId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.profile.update')")
    @PostMapping("/organizations/{organizationId}/contacts/{contactId}/deactivate")
    public Mono<OrganizationContactResponse> deactivateContact(
            @PathVariable String organizationId,
            @PathVariable String contactId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return deactivateOrganizationContactCommandUseCase
                .handle(commandMapper.toDeactivateContactCommand(organizationId, contactId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('directory.profile.create', 'directory.profile.update')")
    @PutMapping("/organizations/{organizationId}/addresses/{addressId}")
    public Mono<AddressResponse> upsertAddress(
            @PathVariable String organizationId,
            @PathVariable String addressId,
            @Valid @RequestBody UpsertAddressRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return upsertOrganizationAddressCommandUseCase
                .handle(commandMapper.toCommand(organizationId, addressId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.profile.update')")
    @PostMapping("/organizations/{organizationId}/addresses/{addressId}/deactivate")
    public Mono<AddressResponse> deactivateAddress(
            @PathVariable String organizationId,
            @PathVariable String addressId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return deactivateOrganizationAddressCommandUseCase
                .handle(commandMapper.toDeactivateAddressCommand(organizationId, addressId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.profile.update')")
    @PostMapping("/organizations/{organizationId}/addresses/{addressId}/default")
    public Mono<AddressResponse> markDefaultAddress(
            @PathVariable String organizationId,
            @PathVariable String addressId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return markDefaultOrganizationAddressCommandUseCase
                .handle(commandMapper.toMarkDefaultAddressCommand(organizationId, addressId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.update')")
    @PostMapping("/organizations/{organizationId}/country-policies")
    public Mono<CountryPolicyResponse> configureRegionalPolicy(
            @PathVariable String organizationId,
            @Valid @RequestBody ConfigureRegionalPolicyRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return configureRegionalPolicyCommandUseCase
                .handle(commandMapper.toCommand(organizationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.read')")
    @PostMapping("/organizations/{organizationId}/country-policies/{countryCode}/apply")
    public Mono<RegionalPolicyApplicationResponse> applyPolicyToOperation(
            @PathVariable String organizationId,
            @PathVariable String countryCode,
            @Valid @RequestBody ApplyRegionalPolicyRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return applyRegionalPolicyToOperationCommandUseCase
                .handle(commandMapper.toCommand(organizationId, countryCode, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.read')")
    @GetMapping("/organizations/{organizationId}")
    public Mono<OrganizationResponse> getOrganization(
            @PathVariable String organizationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getOrganizationQueryUseCase
                .handle(queryMapper.toGetOrganizationQuery(organizationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/internal/organizations/{organizationId}")
    public Mono<OrganizationResponse> getOrganizationInternal(
            @PathVariable String organizationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = resolveInternalPrincipal(authentication, organizationId, null);
        return getOrganizationQueryUseCase
                .handle(queryMapper.toGetOrganizationQuery(organizationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.read')")
    @GetMapping("/organizations/{organizationId}/profile")
    public Mono<OrganizationProfileResponse> getOrganizationProfile(
            @PathVariable String organizationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getOrganizationProfileQueryUseCase
                .handle(queryMapper.toGetOrganizationProfileQuery(organizationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.profile.read')")
    @GetMapping("/organizations/{organizationId}/contacts")
    public Flux<OrganizationContactResponse> listContacts(
            @PathVariable String organizationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listOrganizationContactsQueryUseCase
                .handle(queryMapper.toListOrganizationContactsQuery(organizationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/internal/organizations/{organizationId}/contacts")
    public Flux<OrganizationContactResponse> listContactsInternal(
            @PathVariable String organizationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = resolveInternalPrincipal(authentication, organizationId, null);
        return listOrganizationContactsQueryUseCase
                .handle(queryMapper.toListOrganizationContactsQuery(organizationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.profile.read')")
    @GetMapping("/organizations/{organizationId}/addresses")
    public Flux<AddressResponse> listAddresses(
            @PathVariable String organizationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listOrganizationAddressesQueryUseCase
                .handle(queryMapper.toListOrganizationAddressesQuery(organizationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.profile.read')")
    @GetMapping("/organizations/{organizationId}/addresses/{addressId}/checkout-resolution")
    public Mono<CheckoutAddressResolutionResponse> resolveCheckoutAddress(
            @PathVariable String organizationId,
            @PathVariable String addressId,
            @RequestParam String countryCode,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return resolveCheckoutAddressQueryUseCase
                .handle(queryMapper.toResolveCheckoutAddressQuery(organizationId, addressId, countryCode, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/internal/organizations/{organizationId}/addresses/{addressId}/checkout-resolution")
    public Mono<CheckoutAddressResolutionResponse> resolveCheckoutAddressInternal(
            @PathVariable String organizationId,
            @PathVariable String addressId,
            @RequestParam String countryCode,
            Authentication authentication) {
        IamSecurityPrincipal principal = resolveInternalPrincipal(authentication, organizationId, countryCode);
        return resolveCheckoutAddressQueryUseCase
                .handle(queryMapper.toResolveCheckoutAddressQuery(organizationId, addressId, countryCode, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.read')")
    @GetMapping("/organizations/{organizationId}/country-policies/{countryCode}")
    public Mono<CountryPolicyResponse> getActiveCountryPolicy(
            @PathVariable String organizationId,
            @PathVariable String countryCode,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getActiveCountryPolicyQueryUseCase
                .handle(queryMapper.toGetActiveCountryPolicyQuery(organizationId, countryCode, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/internal/organizations/{organizationId}/country-policies/{countryCode}")
    public Mono<CountryPolicyResponse> getActiveCountryPolicyInternal(
            @PathVariable String organizationId,
            @PathVariable String countryCode,
            Authentication authentication) {
        IamSecurityPrincipal principal = resolveInternalPrincipal(authentication, organizationId, countryCode);
        return getActiveCountryPolicyQueryUseCase
                .handle(queryMapper.toGetActiveCountryPolicyQuery(organizationId, countryCode, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.read')")
    @GetMapping("/organizations/{organizationId}/regional-context/{countryCode}")
    public Mono<OrganizationRegionalContextResponse> getRegionalContext(
            @PathVariable String organizationId,
            @PathVariable String countryCode,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        Mono<OrganizationResponse> organization = getOrganizationQueryUseCase
                .handle(queryMapper.toGetOrganizationQuery(organizationId, principal))
                .map(responseMapper::toResponse);
        Mono<CountryPolicyResponse> policy = getActiveCountryPolicyQueryUseCase
                .handle(queryMapper.toGetActiveCountryPolicyQuery(organizationId, countryCode, principal))
                .map(responseMapper::toResponse);
        return Mono.zip(organization, policy)
                .map(tuple -> new OrganizationRegionalContextResponse(tuple.getT1(), tuple.getT2()));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/internal/organizations/{organizationId}/regional-context/{countryCode}")
    public Mono<OrganizationRegionalContextResponse> getRegionalContextInternal(
            @PathVariable String organizationId,
            @PathVariable String countryCode,
            Authentication authentication) {
        IamSecurityPrincipal principal = resolveInternalPrincipal(authentication, organizationId, countryCode);
        Mono<OrganizationResponse> organization = getOrganizationQueryUseCase
                .handle(queryMapper.toGetOrganizationQuery(organizationId, principal))
                .map(responseMapper::toResponse);
        Mono<CountryPolicyResponse> policy = getActiveCountryPolicyQueryUseCase
                .handle(queryMapper.toGetActiveCountryPolicyQuery(organizationId, countryCode, principal))
                .map(responseMapper::toResponse);
        return Mono.zip(organization, policy)
                .map(tuple -> new OrganizationRegionalContextResponse(tuple.getT1(), tuple.getT2()));
    }

    private IamSecurityPrincipal resolveInternalPrincipal(
            Authentication authentication,
            String organizationId,
            String countryCode) {
        if (authentication != null && authentication.isAuthenticated()) {
            return IamSecurityPrincipal.fromAuthentication(authentication);
        }
        return new IamSecurityPrincipal(
                "svc:internal",
                organizationId == null ? "" : organizationId,
                countryCode == null ? "" : countryCode,
                Set.of("ROLE_ARKA_ADMIN"));
    }

    @PreAuthorize("hasAuthority('directory.organization.read') and hasRole('DIRECTORY_ADMIN')")
    @GetMapping("/admin/summary")
    public Mono<DirectoryAdminSummaryResponse> getDirectoryAdminSummary(Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getDirectoryAdminSummaryQueryUseCase
                .handle(queryMapper.toGetDirectoryAdminSummaryQuery(principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('directory.organization.read')")
    @GetMapping("/organizations/{organizationId}/audit")
    public Mono<DirectoryAuditResponse> getDirectoryAudit(
            @PathVariable String organizationId,
            @RequestParam(defaultValue = "100") int limit,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getDirectoryAuditQueryUseCase
                .handle(queryMapper.toGetDirectoryAuditQuery(organizationId, limit, principal))
                .map(responseMapper::toResponse);
    }
}
