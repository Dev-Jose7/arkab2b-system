package com.arka.directory.infrastructure.adapter.in.web.response;

import java.util.List;

public record OrganizationProfileResponse(
        OrganizationResponse organization,
        OrganizationLegalProfileResponse legalProfile,
        List<OrganizationUserProfileResponse> userProfiles,
        List<OrganizationContactResponse> contacts,
        List<AddressResponse> addresses,
        List<CountryPolicyResponse> activeCountryPolicies) {}
