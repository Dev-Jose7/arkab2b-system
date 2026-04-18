package com.arka.inventory.application.query;

public record GetInventoryAuditQuery(
        String organizationId,
        Integer limit,
        String actorUserId) {}
