package com.arka.reporting.application.exception;

public class ReportingResourceNotFoundException extends ApplicationException {

    public ReportingResourceNotFoundException(String resourceName, String id) {
        super("reporting_recurso_no_encontrado", resourceName + " no encontrado: " + id);
    }
}
