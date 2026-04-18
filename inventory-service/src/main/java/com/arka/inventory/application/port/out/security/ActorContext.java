package com.arka.inventory.application.port.out.security;

public record ActorContext(
        String userId,
        String tenantId,
        boolean inventoryAdmin) {}
