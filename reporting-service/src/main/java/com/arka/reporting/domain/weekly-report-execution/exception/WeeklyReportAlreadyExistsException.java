package com.arka.reporting.domain.weeklyreportexecution.exception;

public class WeeklyReportAlreadyExistsException extends WeeklyReportExecutionDomainException {

    public WeeklyReportAlreadyExistsException() {
        super("reporte_duplicado", "Ya existe una ejecucion para organization+week+reportType");
    }
}
