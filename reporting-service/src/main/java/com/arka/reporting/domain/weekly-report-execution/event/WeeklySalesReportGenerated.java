package com.arka.reporting.domain.weeklyreportexecution.event;

import java.time.Instant;

public final class WeeklySalesReportGenerated extends AbstractWeeklyReportDomainEvent {

    private final String tenantId;
    private final String weekId;
    private final String locationRef;

    public WeeklySalesReportGenerated(String executionId, String tenantId, String weekId, String locationRef, Instant occurredAt) {
        super("WeeklySalesReportGenerated", executionId, occurredAt);
        this.tenantId = tenantId;
        this.weekId = weekId;
        this.locationRef = locationRef;
    }

    public String tenantId() {
        return tenantId;
    }

    public String weekId() {
        return weekId;
    }

    public String locationRef() {
        return locationRef;
    }
}
