package com.arka.reporting.application.exception;

public class RegionalPolicyUnavailableException extends ApplicationException {

    public RegionalPolicyUnavailableException() {
        super("configuracion_pais_no_disponible", "No existe politica regional vigente para la operacion");
    }
}
