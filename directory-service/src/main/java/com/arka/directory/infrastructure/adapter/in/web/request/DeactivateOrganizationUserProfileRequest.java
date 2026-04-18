package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Size;

public record DeactivateOrganizationUserProfileRequest(@Size(max = 200) String reason) {}
