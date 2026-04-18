package com.arka.reporting.application.port.out.persistence;

import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.ExecutionId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.OrganizationId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import reactor.core.publisher.Mono;

public interface WeeklyReportExecutionPersistencePort {

    Mono<WeeklyReportExecution> create(WeeklyReportExecution execution);

    Mono<WeeklyReportExecution> update(WeeklyReportExecution execution);

    Mono<WeeklyReportExecution> findById(OrganizationId organizationId, ExecutionId executionId);

    Mono<WeeklyReportExecution> findByWeekAndType(OrganizationId organizationId, WeekId weekId, String reportType);

    Mono<Boolean> existsRunningRebuild(OrganizationId organizationId);
}
