package com.arka.reporting.domain.analyticfact.exception;

public class InvalidAnalyticFactTransitionException extends AnalyticFactDomainException {

    public InvalidAnalyticFactTransitionException(String from, String to) {
        super("transicion_fact_invalida", "No se permite transicion " + from + " -> " + to);
    }
}
