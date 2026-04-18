package com.arka.reporting.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactType;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.analyticfact.valueobject.TenantId;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.ReportType;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import com.arka.reporting.infrastructure.adapter.out.persistence.mapper.ReportingRowMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReportingRowMapperTest {

    private final ReportingRowMapper mapper = new ReportingRowMapper();

    @Test
    void shouldRoundTripAnalyticFactBetweenDomainAndRow() {
        Instant now = Instant.parse("2026-04-05T10:00:00Z");
        AnalyticFact fact = AnalyticFact.capture(
                TenantId.of("tenant-demo"),
                SourceEventId.of("evt-1"),
                "order.confirmed",
                AnalyticFactType.SALES,
                "{}",
                now,
                now);
        fact.normalize("{}", now.plusSeconds(1));

        AnalyticFact mapped = mapper.toDomain(mapper.toRow(fact, "2026-W14"));

        assertEquals(fact.factId().value(), mapped.factId().value());
        assertEquals(fact.tenantId().value(), mapped.tenantId().value());
        assertEquals(fact.factStatus(), mapped.factStatus());
        assertEquals("order.confirmed", mapped.eventType());
    }

    @Test
    void shouldRoundTripWeeklyExecutionBetweenDomainAndRow() {
        Instant now = Instant.parse("2026-04-05T10:00:00Z");
        WeeklyReportExecution execution = WeeklyReportExecution.pending(
                com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId.of("tenant-demo"),
                WeekId.of("2026-W14"),
                ReportType.SALES,
                now);

        WeeklyReportExecution mapped = mapper.toDomain(mapper.toRow(execution));

        assertEquals(execution.executionId().value(), mapped.executionId().value());
        assertEquals(execution.status(), mapped.status());
        assertEquals(execution.weekId().value(), mapped.weekId().value());
    }
}
