package com.arka.directory.application.command;

public record UpsertOrganizationLegalProfileCommand(
        String organizationId,
        String taxIdType,
        String taxId,
        String fiscalRegime,
        String legalRepresentative,
        String countryCode,
        String verificationStatus,
        String actorUserId,
        String actorOrganizationId) {}
