package com.arka.directory.application.port.out.security;

public record ActorContext(
        String userId,
        String organizationId,
        String countryCode,
        boolean directoryAdmin) {}
