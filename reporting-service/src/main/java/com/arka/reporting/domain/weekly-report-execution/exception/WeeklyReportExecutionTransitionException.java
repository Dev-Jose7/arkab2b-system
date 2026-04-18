package com.arka.reporting.domain.weeklyreportexecution.exception;

public class WeeklyReportExecutionTransitionException extends WeeklyReportExecutionDomainException {

    public WeeklyReportExecutionTransitionException(String from, String to) {
        super("weekly_execution_transicion_invalida", "No se permite transicion " + from + " -> " + to);
    }
}
