package com.arka.reporting.application.port.out.security;

public record ActorContext(
        String actorId,
        String tenantId,
        String countryCode,
        boolean admin,
        boolean trustedService) {
}
