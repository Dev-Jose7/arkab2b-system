package com.arka.reporting.application.port.out.persistence;

import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.ExecutionId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import reactor.core.publisher.Mono;

public interface WeeklyReportExecutionPersistencePort {

    Mono<WeeklyReportExecution> create(WeeklyReportExecution execution);

    Mono<WeeklyReportExecution> update(WeeklyReportExecution execution);

    Mono<WeeklyReportExecution> findById(TenantId tenantId, ExecutionId executionId);

    Mono<WeeklyReportExecution> findByWeekAndType(TenantId tenantId, WeekId weekId, String reportType);

    Mono<Boolean> existsRunningRebuild(TenantId tenantId);
}
