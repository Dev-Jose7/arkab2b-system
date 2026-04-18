package com.arka.order.application.query;

public record GetOrderAuditQuery(
        String organizationId,

        String orderId,
        Integer limit,
        String actorUserId) {}
