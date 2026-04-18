package com.arka.directory.infrastructure.adapter.in.web.response;

public record OrganizationRegionalContextResponse(
        OrganizationResponse organization,
        CountryPolicyResponse countryPolicy) {}
