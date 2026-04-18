package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record OperationsKpiResponse(
        String projectionId,
        String organizationId,
        String period,
        String kpiName,
        BigDecimal kpiValue) {}
