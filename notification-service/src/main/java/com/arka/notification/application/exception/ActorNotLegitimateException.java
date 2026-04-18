package com.arka.notification.application.exception;

public class ActorNotLegitimateException extends ApplicationException {

    public ActorNotLegitimateException() {
        super("actor_no_legitimo", "Actor no legitimo para ejecutar la operacion");
    }
}
