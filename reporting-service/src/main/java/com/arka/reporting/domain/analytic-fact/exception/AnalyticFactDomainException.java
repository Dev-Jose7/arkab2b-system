package com.arka.reporting.domain.analyticfact.exception;

import com.arka.reporting.domain.shared.exception.DomainException;

public class AnalyticFactDomainException extends DomainException {

    public AnalyticFactDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}
