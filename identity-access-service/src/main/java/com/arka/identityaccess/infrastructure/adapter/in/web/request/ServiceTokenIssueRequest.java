package com.arka.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ServiceTokenIssueRequest(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        String audience,
        List<String> scopes,
        String organizationId,
        String countryCode) {}
