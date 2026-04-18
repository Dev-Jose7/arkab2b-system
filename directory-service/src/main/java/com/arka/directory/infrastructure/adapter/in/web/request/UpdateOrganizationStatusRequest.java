package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrganizationStatusRequest(@NotBlank String status) {}
