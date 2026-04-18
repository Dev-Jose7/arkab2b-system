package com.arka.reporting.domain.weeklyreportexecution.repository;

import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import reactor.core.publisher.Mono;

public interface WeeklyReportExecutionRepository {

    Mono<WeeklyReportExecution> save(WeeklyReportExecution execution);
}
