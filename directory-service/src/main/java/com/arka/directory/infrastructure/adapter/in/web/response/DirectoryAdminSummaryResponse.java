package com.arka.directory.infrastructure.adapter.in.web.response;

public record DirectoryAdminSummaryResponse(
        long organizationsTotal,
        long organizationsActive,
        long organizationsSuspended,
        long organizationsInactive,
        long activeCountryPolicies,
        long activeContacts,
        long activeAddresses) {}
