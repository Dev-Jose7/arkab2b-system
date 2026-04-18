package com.arka.notification.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record ErrorResponse(String code, String message, Instant timestamp) {
}
