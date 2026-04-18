package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertOrganizationContactRequest(
        @NotBlank @Size(max = 30) String contactType,
        @Size(max = 100) String label,
        @NotBlank @Size(max = 255) String value,
        @Size(max = 255) String valueMasked,
        Boolean primary,
        @Size(max = 30) String status) {}
