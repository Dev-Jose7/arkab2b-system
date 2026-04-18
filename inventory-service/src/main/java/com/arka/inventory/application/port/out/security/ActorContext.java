package com.arka.inventory.application.port.out.security;

public record ActorContext(
        String userId,
        String organizationId,
        boolean inventoryAdmin,
        boolean trustedService) {}
