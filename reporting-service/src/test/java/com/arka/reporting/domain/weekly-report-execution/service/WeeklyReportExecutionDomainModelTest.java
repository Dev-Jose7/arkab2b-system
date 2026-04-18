package com.arka.reporting.domain.weeklyreportexecution.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.reporting.domain.weeklyreportexecution.aggregate.WeeklyReportExecutionAggregate;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.ReportType;
import com.arka.reporting.domain.weeklyreportexecution.exception.WeeklyReportExecutionArtifactRequiredException;
import com.arka.reporting.domain.weeklyreportexecution.exception.WeeklyReportExecutionTransitionException;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class WeeklyReportExecutionDomainModelTest {

    @Test
    void shouldRequireArtifactLocationToCompleteExecution() {
        Instant now = Instant.parse("2026-04-07T00:00:00Z");
        WeeklyReportExecution execution = WeeklyReportExecution.pending(
                TenantId.of("tenant-demo"),
                WeekId.of("2026-W14"),
                ReportType.SALES,
                now);

        WeeklyReportExecutionAggregate aggregate = WeeklyReportExecutionAggregate.rehydrate(execution);
        aggregate.start(now.plusSeconds(1));

        assertThrows(
                WeeklyReportExecutionArtifactRequiredException.class,
                () -> aggregate.complete(" ", now.plusSeconds(2)));
    }

    @Test
    void shouldEmitWeeklyReportGeneratedEventOnComplete() {
        Instant now = Instant.parse("2026-04-07T00:00:00Z");
        WeeklyReportExecution execution = WeeklyReportExecution.pending(
                TenantId.of("tenant-demo"),
                WeekId.of("2026-W14"),
                ReportType.REPLENISHMENT,
                now);

        WeeklyReportExecutionAggregate aggregate = WeeklyReportExecutionAggregate.rehydrate(execution);
        aggregate.start(now.plusSeconds(1));
        aggregate.complete("stub://artifact", now.plusSeconds(2));

        assertEquals("COMPLETED", aggregate.execution().status().name());
        assertEquals(1, aggregate.pullDomainEvents().size());
    }

    @Test
    void shouldRejectInvalidTransitionWhenStartAfterCompleted() {
        Instant now = Instant.parse("2026-04-07T00:00:00Z");
        WeeklyReportExecution execution = WeeklyReportExecution.pending(
                TenantId.of("tenant-demo"),
                WeekId.of("2026-W14"),
                ReportType.SALES,
                now);

        WeeklyReportExecutionAggregate aggregate = WeeklyReportExecutionAggregate.rehydrate(execution);
        aggregate.start(now.plusSeconds(1));
        aggregate.complete("stub://artifact", now.plusSeconds(2));

        assertThrows(
                WeeklyReportExecutionTransitionException.class,
                () -> aggregate.start(now.plusSeconds(3)));
    }
}
