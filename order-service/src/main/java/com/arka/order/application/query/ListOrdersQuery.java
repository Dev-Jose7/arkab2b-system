package com.arka.order.application.query;

import java.time.Instant;

public record ListOrdersQuery(
        String organizationId,

        String status,
        Instant createdFrom,
        Instant createdTo,
        Integer limit,
        String actorUserId) {}
