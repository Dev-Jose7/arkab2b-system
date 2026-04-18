package com.arka.reporting.domain.weeklyreportexecution.exception;

public class RebuildInProgressException extends WeeklyReportExecutionDomainException {

    public RebuildInProgressException() {
        super("rebuild_in_progress", "Existe una ejecucion de rebuild en estado RUNNING");
    }
}
