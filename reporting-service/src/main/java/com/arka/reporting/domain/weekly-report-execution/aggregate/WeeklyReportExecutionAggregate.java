package com.arka.reporting.domain.weeklyreportexecution.aggregate;

import com.arka.reporting.domain.shared.event.DomainEvent;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.ReportType;
import com.arka.reporting.domain.weeklyreportexecution.event.WeeklyReplenishmentReportGenerated;
import com.arka.reporting.domain.weeklyreportexecution.event.WeeklySalesReportGenerated;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class WeeklyReportExecutionAggregate {

    private final WeeklyReportExecution execution;
    private final List<DomainEvent> domainEvents;

    private WeeklyReportExecutionAggregate(WeeklyReportExecution execution, List<DomainEvent> domainEvents) {
        this.execution = execution;
        this.domainEvents = domainEvents;
    }

    public static WeeklyReportExecutionAggregate rehydrate(WeeklyReportExecution execution) {
        return new WeeklyReportExecutionAggregate(execution, new ArrayList<>());
    }

    public void start(Instant now) {
        execution.start(now);
    }

    public void complete(String locationRef, Instant now) {
        execution.complete(locationRef, now);
        if (execution.reportType() == ReportType.SALES) {
            domainEvents.add(new WeeklySalesReportGenerated(
                    execution.executionId().value(),
                    execution.organizationId().value(),
                    execution.weekId().value(),
                    locationRef,
                    now));
        } else if (execution.reportType() == ReportType.REPLENISHMENT) {
            domainEvents.add(new WeeklyReplenishmentReportGenerated(
                    execution.executionId().value(),
                    execution.organizationId().value(),
                    execution.weekId().value(),
                    locationRef,
                    now));
        }
    }

    public void fail(String errorCode, String errorMessage, Instant now) {
        execution.fail(errorCode, errorMessage, now);
    }

    public WeeklyReportExecution execution() {
        return execution;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
