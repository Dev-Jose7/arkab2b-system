package com.arka.directory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record OrganizationContactResponse(
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
