package com.arka.directory.application.mapper.result;

import com.arka.directory.application.result.AddressResult;
import com.arka.directory.application.result.CountryPolicyResult;
import com.arka.directory.application.result.OrganizationContactResult;
import com.arka.directory.application.result.OrganizationLegalProfileResult;
import com.arka.directory.application.result.OrganizationResult;
import com.arka.directory.application.result.OrganizationUserProfileResult;
import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.entity.Address;
import com.arka.directory.domain.organizationcontext.entity.OrganizationContact;
import com.arka.directory.domain.organizationcontext.entity.OrganizationLegalProfile;
import com.arka.directory.domain.organizationcontext.entity.OrganizationUserProfile;
import org.springframework.stereotype.Component;

@Component
public class DirectoryResultMapper {

    public OrganizationResult toResult(Organization organization) {
        return new OrganizationResult(
                organization.id().value(),
                organization.legalName(),
                organization.tradeName(),
                organization.countryCode().value(),
                organization.currencyCode(),
                organization.timezone(),
                organization.segmentTier(),
                organization.status().name(),
                organization.createdAt(),
                organization.updatedAt());
    }

    public OrganizationLegalProfileResult toResult(OrganizationLegalProfile profile) {
        return new OrganizationLegalProfileResult(
                profile.legalProfileId(),
                profile.organizationId(),
                profile.taxIdType(),
                profile.taxId(),
                profile.fiscalRegime(),
                profile.legalRepresentative(),
                profile.countryCode(),
                profile.verificationStatus().name(),
                profile.verifiedAt(),
                profile.createdAt(),
                profile.updatedAt());
    }

    public OrganizationUserProfileResult toResult(OrganizationUserProfile userProfile) {
        return new OrganizationUserProfileResult(
                userProfile.userProfileId(),
                userProfile.organizationId(),
                userProfile.iamUserId(),
                userProfile.displayName(),
                userProfile.jobTitle(),
                userProfile.department(),
                userProfile.locale(),
                userProfile.timezone(),
                userProfile.roleReference(),
                userProfile.ownershipScope(),
                userProfile.status().name(),
                userProfile.createdAt(),
                userProfile.updatedAt());
    }

    public OrganizationContactResult toResult(OrganizationContact contact) {
        return new OrganizationContactResult(
                contact.contactId(),
                contact.organizationId(),
                contact.contactType().name(),
                contact.label(),
                contact.valueNormalized(),
                contact.valueMasked(),
                contact.primary(),
                contact.status().name(),
                contact.createdAt(),
                contact.updatedAt());
    }

    public AddressResult toResult(Address address) {
        return new AddressResult(
                address.addressId(),
                address.organizationId(),
                address.addressType().name(),
                address.alias(),
                address.line1(),
                address.line2(),
                address.city(),
                address.stateRegion(),
                address.postalCode(),
                address.countryCode(),
                address.reference(),
                address.latitude(),
                address.longitude(),
                address.isDefault(),
                address.status().name(),
                address.validationStatus().name(),
                address.validatedAt(),
                address.createdAt(),
                address.updatedAt());
    }

    public CountryPolicyResult toResult(CountryPolicy policy) {
        return new CountryPolicyResult(
                policy.policyId(),
                policy.organizationId().value(),
                policy.countryCode().value(),
                policy.policyVersion().value(),
                policy.currencyCode(),
                policy.weekStartsOn().name(),
                policy.weeklyCutoffLocalTime(),
                policy.timezone(),
                policy.reportingRetentionDays(),
                policy.requiresVerifiedAddress(),
                policy.effectiveFrom(),
                policy.effectiveTo(),
                policy.status().name(),
                policy.createdAt(),
                policy.updatedAt());
    }
}
