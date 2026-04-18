package com.arka.catalog.application.exception;

public class ActorNotLegitimateException extends ApplicationException {

    public ActorNotLegitimateException() {
        super("actor_no_legitimo", "El actor no fue validado como legitimo para la operacion");
    }
}
