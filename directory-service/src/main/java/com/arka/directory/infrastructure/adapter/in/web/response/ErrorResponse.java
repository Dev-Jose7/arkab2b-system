package com.arka.directory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp) {}
