package com.arka.reporting.domain.weeklyreportexecution.event;

import java.time.Instant;

public final class WeeklySalesReportGenerated extends AbstractWeeklyReportDomainEvent {

    private final String organizationId;
    private final String weekId;
    private final String locationRef;

    public WeeklySalesReportGenerated(String executionId, String organizationId, String weekId, String locationRef, Instant occurredAt) {
        super("WeeklySalesReportGenerated", executionId, occurredAt);
        this.organizationId = organizationId;
        this.weekId = weekId;
        this.locationRef = locationRef;
    }

    public String organizationId() {
        return organizationId;
    }

    public String weekId() {
        return weekId;
    }

    public String locationRef() {
        return locationRef;
    }
}
