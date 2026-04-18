package com.arka.notification.application.port.out.security;

public record ActorContext(
        String actorId,
        String organizationId,
        String countryCode,
        boolean admin,
        boolean trustedService) {
}
