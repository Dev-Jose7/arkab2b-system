package com.arka.inventory.application.query;

public record GetInventoryAuditQuery(
        String tenantId,
        Integer limit,
        String actorUserId) {}
