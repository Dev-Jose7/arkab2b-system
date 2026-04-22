package com.arka.directory.infrastructure.adapter.out.persistence.mapper;

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
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.AddressRow;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationContactRow;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationCountryPolicyRow;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationLegalProfileRow;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationRow;
import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationUserProfileRow;
import org.springframework.stereotype.Component;

@Component
public class DirectoryRowMapper {

    public Organization toDomain(OrganizationRow row) {
        return Organization.rehydrate(
                OrganizationId.of(row.organizationId()),
                row.legalName(),
                row.tradeName(),
                CountryCode.of(row.countryCode()),
                row.currencyCode(),
                row.timezone(),
                row.segmentTier(),
                OrganizationStatus.valueOf(row.status()),
                row.createdAt(),
                row.updatedAt());
    }

    public OrganizationRow toRow(Organization organization) {
        return new OrganizationRow(
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

    public OrganizationLegalProfile toDomain(OrganizationLegalProfileRow row) {
        return new OrganizationLegalProfile(
                row.legalProfileId(),
                row.organizationId(),
                row.taxIdType(),
                row.taxId(),
                row.fiscalRegime(),
                row.legalRepresentative(),
                row.countryCode(),
                OrganizationLegalProfileStatus.valueOf(row.verificationStatus()),
                row.verifiedAt(),
                row.createdAt(),
                row.updatedAt());
    }

    public OrganizationLegalProfileRow toRow(OrganizationLegalProfile legalProfile) {
        return new OrganizationLegalProfileRow(
                legalProfile.legalProfileId(),
                legalProfile.organizationId(),
                legalProfile.taxIdType(),
                legalProfile.taxId(),
                legalProfile.fiscalRegime(),
                legalProfile.legalRepresentative(),
                legalProfile.countryCode(),
                legalProfile.verificationStatus().name(),
                legalProfile.verifiedAt(),
                legalProfile.createdAt(),
                legalProfile.updatedAt());
    }

    public OrganizationUserProfile toDomain(OrganizationUserProfileRow row) {
        return new OrganizationUserProfile(
                row.userProfileId(),
                row.organizationId(),
                row.iamUserId(),
                row.displayName(),
                row.jobTitle(),
                row.department(),
                row.locale(),
                row.timezone(),
                row.roleReference(),
                row.ownershipScope(),
                OrganizationUserProfileStatus.valueOf(row.status()),
                row.createdAt(),
                row.updatedAt());
    }

    public OrganizationUserProfileRow toRow(OrganizationUserProfile userProfile) {
        return new OrganizationUserProfileRow(
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

    public OrganizationContact toDomain(OrganizationContactRow row) {
        return new OrganizationContact(
                row.contactId(),
                row.organizationId(),
                ContactType.valueOf(row.contactType()),
                row.label(),
                row.valueNormalized(),
                row.valueMasked(),
                row.isPrimary(),
                OrganizationContactStatus.valueOf(row.status()),
                row.createdAt(),
                row.updatedAt());
    }

    public OrganizationContactRow toRow(OrganizationContact contact) {
        return new OrganizationContactRow(
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

    public Address toDomain(AddressRow row) {
        return new Address(
                row.addressId(),
                row.organizationId(),
                AddressType.valueOf(row.addressType()),
                row.alias(),
                row.line1(),
                row.line2(),
                row.city(),
                row.stateRegion(),
                row.postalCode(),
                row.countryCode(),
                row.reference(),
                row.latitude(),
                row.longitude(),
                row.isDefault(),
                AddressStatus.valueOf(row.status()),
                AddressValidationStatus.valueOf(row.validationStatus()),
                row.validatedAt(),
                row.createdAt(),
                row.updatedAt());
    }

    public AddressRow toRow(Address address) {
        return new AddressRow(
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

    public CountryPolicy toDomain(OrganizationCountryPolicyRow row) {
        return CountryPolicy.rehydrate(
                row.policyId(),
                OrganizationId.of(row.organizationId()),
                CountryCode.of(row.countryCode()),
                PolicyVersion.of(row.policyVersion()),
                row.currencyCode(),
                WeekStartsOn.valueOf(row.weekStartsOn()),
                row.weeklyCutoffLocalTime(),
                row.timezone(),
                row.reportingRetentionDays(),
                row.requiresVerifiedAddress() == null ? true : row.requiresVerifiedAddress(),
                row.effectiveFrom(),
                row.effectiveTo(),
                CountryPolicyStatus.valueOf(row.status()),
                row.createdAt(),
                row.updatedAt());
    }

    public OrganizationCountryPolicyRow toRow(CountryPolicy policy) {
        return new OrganizationCountryPolicyRow(
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
