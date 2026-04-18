package com.arka.directory.application.result;

import java.util.List;

public record OrganizationProfileResult(
        OrganizationResult organization,
        OrganizationLegalProfileResult legalProfile,
        List<OrganizationUserProfileResult> userProfiles,
        List<OrganizationContactResult> contacts,
        List<AddressResult> addresses,
        List<CountryPolicyResult> activeCountryPolicies) {}
