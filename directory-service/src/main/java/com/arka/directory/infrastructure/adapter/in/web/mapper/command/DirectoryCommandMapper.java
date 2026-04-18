package com.arka.directory.infrastructure.adapter.in.web.mapper.command;

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
import com.arka.directory.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.directory.infrastructure.adapter.in.web.request.ApplyRegionalPolicyRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.ConfigureRegionalPolicyRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.CreateOrganizationRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.DeactivateOrganizationUserProfileRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpdateOrganizationStatusRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertAddressRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertOrganizationContactRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertOrganizationLegalProfileRequest;
import com.arka.directory.infrastructure.adapter.in.web.request.UpsertOrganizationUserProfileRequest;
import org.springframework.stereotype.Component;

@Component
public class DirectoryCommandMapper {

    public CreateOrganizationCommand toCommand(CreateOrganizationRequest request, IamSecurityPrincipal principal) {
        return new CreateOrganizationCommand(
                request.organizationCode(),
                request.legalName(),
                request.tradeName(),
                request.countryCode(),
                request.currencyCode(),
                request.timezone(),
                request.segmentTier(),
                principal.userId(),
                principal.organizationId());
    }

    public UpdateOrganizationStatusCommand toCommand(
            String organizationId,
            UpdateOrganizationStatusRequest request,
            IamSecurityPrincipal principal) {
        return new UpdateOrganizationStatusCommand(
                organizationId,
                request.status(),
                principal.userId(),
                principal.organizationId());
    }

    public UpsertOrganizationLegalProfileCommand toCommand(
            String organizationId,
            UpsertOrganizationLegalProfileRequest request,
            IamSecurityPrincipal principal) {
        return new UpsertOrganizationLegalProfileCommand(
                organizationId,
                request.taxIdType(),
                request.taxId(),
                request.fiscalRegime(),
                request.legalRepresentative(),
                request.countryCode(),
                request.verificationStatus(),
                principal.userId(),
                principal.organizationId());
    }

    public UpsertOrganizationUserProfileCommand toCommand(
            String organizationId,
            String iamUserId,
            UpsertOrganizationUserProfileRequest request,
            IamSecurityPrincipal principal) {
        return new UpsertOrganizationUserProfileCommand(
                organizationId,
                request.userProfileId(),
                iamUserId,
                request.displayName(),
                request.jobTitle(),
                request.department(),
                request.locale(),
                request.timezone(),
                request.roleReference(),
                request.ownershipScope(),
                request.status(),
                principal.userId(),
                principal.organizationId());
    }

    public DeactivateOrganizationUserProfileCommand toCommand(
            String organizationId,
            String iamUserId,
            DeactivateOrganizationUserProfileRequest request,
            IamSecurityPrincipal principal) {
        return new DeactivateOrganizationUserProfileCommand(
                organizationId,
                iamUserId,
                request == null ? null : request.reason(),
                principal.userId(),
                principal.organizationId());
    }

    public HandleIamUserBlockedCommand toHandleIamUserBlockedCommand(
            String organizationId,
            String iamUserId,
            IamSecurityPrincipal principal) {
        return new HandleIamUserBlockedCommand(
                organizationId,
                iamUserId,
                principal.userId(),
                principal.organizationId());
    }

    public UpsertOrganizationContactCommand toCommand(
            String organizationId,
            String contactId,
            UpsertOrganizationContactRequest request,
            IamSecurityPrincipal principal) {
        return new UpsertOrganizationContactCommand(
                organizationId,
                contactId,
                request.contactType(),
                request.label(),
                request.value(),
                request.valueMasked(),
                request.primary(),
                request.status(),
                principal.userId(),
                principal.organizationId());
    }

    public DeactivateOrganizationContactCommand toDeactivateContactCommand(
            String organizationId,
            String contactId,
            IamSecurityPrincipal principal) {
        return new DeactivateOrganizationContactCommand(
                organizationId,
                contactId,
                principal.userId(),
                principal.organizationId());
    }

    public UpsertAddressCommand toCommand(
            String organizationId,
            String addressId,
            UpsertAddressRequest request,
            IamSecurityPrincipal principal) {
        return new UpsertAddressCommand(
                organizationId,
                addressId,
                request.addressType(),
                request.alias(),
                request.line1(),
                request.line2(),
                request.city(),
                request.stateRegion(),
                request.postalCode(),
                request.countryCode(),
                request.reference(),
                request.latitude(),
                request.longitude(),
                request.isDefault(),
                request.status(),
                request.validationStatus(),
                principal.userId(),
                principal.organizationId());
    }

    public DeactivateAddressCommand toDeactivateAddressCommand(
            String organizationId,
            String addressId,
            IamSecurityPrincipal principal) {
        return new DeactivateAddressCommand(
                organizationId,
                addressId,
                principal.userId(),
                principal.organizationId());
    }

    public MarkDefaultAddressCommand toMarkDefaultAddressCommand(
            String organizationId,
            String addressId,
            IamSecurityPrincipal principal) {
        return new MarkDefaultAddressCommand(
                organizationId,
                addressId,
                null,
                principal.userId(),
                principal.organizationId());
    }

    public ConfigureRegionalPolicyCommand toCommand(
            String organizationId,
            ConfigureRegionalPolicyRequest request,
            IamSecurityPrincipal principal) {
        return new ConfigureRegionalPolicyCommand(
                organizationId,
                request.countryCode(),
                request.currencyCode(),
                request.weekStartsOn(),
                request.weeklyCutoffLocalTime(),
                request.timezone(),
                request.reportingRetentionDays(),
                request.requiresVerifiedAddress(),
                request.effectiveFrom(),
                principal.userId(),
                principal.organizationId());
    }

    public ApplyRegionalPolicyToOperationCommand toCommand(
            String organizationId,
            String countryCode,
            ApplyRegionalPolicyRequest request,
            IamSecurityPrincipal principal) {
        return new ApplyRegionalPolicyToOperationCommand(
                organizationId,
                countryCode,
                request.operationCode(),
                principal.userId(),
                principal.organizationId());
    }
}
