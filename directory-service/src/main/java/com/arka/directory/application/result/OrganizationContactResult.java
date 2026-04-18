package com.arka.directory.application.result;

import java.time.Instant;

public record OrganizationContactResult(
        String contactId,
        String organizationId,
        String contactType,
        String label,
        String value,
        String valueMasked,
        boolean primary,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
