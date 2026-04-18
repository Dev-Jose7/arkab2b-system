package com.arka.reporting.domain.weeklyreportexecution.valueobject;

import java.util.List;

public record WeeklyReplenishmentReport(
        String organizationId,
        String weekId,
        long highRiskCount,
        List<String> prioritizedSkus) {
}
