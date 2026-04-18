package com.arka.reporting.domain.weeklyreportexecution.exception;

import com.arka.reporting.domain.shared.exception.DomainException;

public class WeeklyReportExecutionDomainException extends DomainException {

    public WeeklyReportExecutionDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}
