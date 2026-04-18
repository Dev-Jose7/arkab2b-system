package com.arka.reporting.infrastructure.adapter.out.persistence;

import com.arka.reporting.application.port.out.persistence.WeeklyReportExecutionPersistencePort;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.repository.WeeklyReportExecutionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DomainWeeklyReportExecutionRepositoryAdapter implements WeeklyReportExecutionRepository {

    private final WeeklyReportExecutionPersistencePort persistencePort;

    public DomainWeeklyReportExecutionRepositoryAdapter(WeeklyReportExecutionPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Mono<WeeklyReportExecution> save(WeeklyReportExecution execution) {
        return persistencePort
                .findById(execution.tenantId(), execution.executionId())
                .flatMap(existing -> persistencePort.update(execution))
                .switchIfEmpty(persistencePort.create(execution));
    }
}
