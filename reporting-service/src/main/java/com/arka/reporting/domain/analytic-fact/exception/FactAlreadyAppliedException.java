package com.arka.reporting.domain.analyticfact.exception;

public class FactAlreadyAppliedException extends AnalyticFactDomainException {

    public FactAlreadyAppliedException() {
        super("fact_ya_aplicado", "El hecho analitico ya fue aplicado previamente");
    }
}
