package com.arka.directory.application.result;

public record DirectoryAdminSummaryResult(
        long organizationsTotal,
        long organizationsActive,
        long organizationsSuspended,
        long organizationsInactive,
        long activeCountryPolicies,
        long activeContacts,
        long activeAddresses) {}
