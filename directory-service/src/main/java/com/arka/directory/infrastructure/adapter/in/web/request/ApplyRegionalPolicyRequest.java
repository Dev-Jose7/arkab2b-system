package com.arka.directory.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyRegionalPolicyRequest(@NotBlank @Size(max = 100) String operationCode) {}
