package com.arka.directory.infrastructure.adapter.in.web.mapper.response;

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
import com.arka.directory.infrastructure.adapter.in.web.response.AddressResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.CheckoutAddressResolutionResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.CountryPolicyResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.DirectoryAdminSummaryResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.DirectoryAuditEntryResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.DirectoryAuditResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationContactResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationLegalProfileResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationProfileResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.OrganizationUserProfileResponse;
import com.arka.directory.infrastructure.adapter.in.web.response.RegionalPolicyApplicationResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DirectoryResponseMapper {

    public OrganizationResponse toResponse(OrganizationResult result) {
        return new OrganizationResponse(
                result.organizationId(),
                result.organizationCode(),
                result.legalName(),
                result.tradeName(),
                result.countryCode(),
                result.currencyCode(),
                result.timezone(),
                result.segmentTier(),
                result.status(),
                result.createdAt(),
                result.updatedAt());
    }

    public OrganizationLegalProfileResponse toResponse(OrganizationLegalProfileResult result) {
        return new OrganizationLegalProfileResponse(
                result.legalProfileId(),
                result.organizationId(),
                result.taxIdType(),
                result.taxId(),
                result.fiscalRegime(),
                result.legalRepresentative(),
                result.countryCode(),
                result.verificationStatus(),
                result.verifiedAt(),
                result.createdAt(),
                result.updatedAt());
    }

    public OrganizationUserProfileResponse toResponse(OrganizationUserProfileResult result) {
        return new OrganizationUserProfileResponse(
                result.userProfileId(),
                result.organizationId(),
                result.iamUserId(),
                result.displayName(),
                result.jobTitle(),
                result.department(),
                result.locale(),
                result.timezone(),
                result.roleReference(),
                result.ownershipScope(),
                result.status(),
                result.createdAt(),
                result.updatedAt());
    }

    public OrganizationContactResponse toResponse(OrganizationContactResult result) {
        return new OrganizationContactResponse(
                result.contactId(),
                result.organizationId(),
                result.contactType(),
                result.label(),
                result.value(),
                result.valueMasked(),
                result.primary(),
                result.status(),
                result.createdAt(),
                result.updatedAt());
    }

    public AddressResponse toResponse(AddressResult result) {
        return new AddressResponse(
                result.addressId(),
                result.organizationId(),
                result.addressType(),
                result.alias(),
                result.line1(),
                result.line2(),
                result.city(),
                result.stateRegion(),
                result.postalCode(),
                result.countryCode(),
                result.reference(),
                result.latitude(),
                result.longitude(),
                result.isDefault(),
                result.status(),
                result.validationStatus(),
                result.validatedAt(),
                result.createdAt(),
                result.updatedAt());
    }

    public CountryPolicyResponse toResponse(CountryPolicyResult result) {
        return new CountryPolicyResponse(
                result.policyId(),
                result.organizationId(),
                result.countryCode(),
                result.policyVersion(),
                result.currencyCode(),
                result.weekStartsOn(),
                result.weeklyCutoffLocalTime(),
                result.timezone(),
                result.reportingRetentionDays(),
                result.requiresVerifiedAddress(),
                result.effectiveFrom(),
                result.effectiveTo(),
                result.status(),
                result.createdAt(),
                result.updatedAt());
    }

    public OrganizationProfileResponse toResponse(OrganizationProfileResult result) {
        return new OrganizationProfileResponse(
                toResponse(result.organization()),
                result.legalProfile() == null ? null : toResponse(result.legalProfile()),
                result.userProfiles().stream().map(this::toResponse).toList(),
                result.contacts().stream().map(this::toResponse).toList(),
                result.addresses().stream().map(this::toResponse).toList(),
                result.activeCountryPolicies().stream().map(this::toResponse).toList());
    }

    public CheckoutAddressResolutionResponse toResponse(CheckoutAddressResolutionResult result) {
        return new CheckoutAddressResolutionResponse(
                toResponse(result.address()),
                toResponse(result.countryPolicy()),
                result.resolutionStatus());
    }

    public DirectoryAdminSummaryResponse toResponse(DirectoryAdminSummaryResult result) {
        return new DirectoryAdminSummaryResponse(
                result.organizationsTotal(),
                result.organizationsActive(),
                result.organizationsSuspended(),
                result.organizationsInactive(),
                result.activeCountryPolicies(),
                result.activeContacts(),
                result.activeAddresses());
    }

    public DirectoryAuditResponse toResponse(DirectoryAuditResult result) {
        List<DirectoryAuditEntryResponse> entries = result.entries().stream().map(this::toResponse).toList();
        return new DirectoryAuditResponse(result.organizationId(), entries);
    }

    public DirectoryAuditEntryResponse toResponse(DirectoryAuditEntryResult result) {
        return new DirectoryAuditEntryResponse(
                result.auditId(),
                result.organizationId(),
                result.actorUserId(),
                result.actionType(),
                result.targetType(),
                result.targetId(),
                result.outcome(),
                result.payload(),
                result.createdAt());
    }

    public RegionalPolicyApplicationResponse toResponse(RegionalPolicyApplicationResult result) {
        return new RegionalPolicyApplicationResponse(
                result.organizationId(),
                result.countryCode(),
                result.policyVersion(),
                result.operationCode(),
                result.currencyCode(),
                result.timezone(),
                result.weeklyCutoffLocalTime(),
                result.reportingRetentionDays(),
                result.status());
    }
}
