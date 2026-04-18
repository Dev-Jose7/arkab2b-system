package com.arka.order.application.result;

import java.util.List;

public record OrderAuditResult(
        String organizationId,

        String orderId,
        List<OrderAuditEntryResult> entries) {}
