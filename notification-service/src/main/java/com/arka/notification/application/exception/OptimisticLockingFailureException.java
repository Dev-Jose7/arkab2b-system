package com.arka.notification.application.exception;

public class OptimisticLockingFailureException extends ApplicationException {

    public OptimisticLockingFailureException() {
        super("optimistic_locking_failure", "Conflicto de version al actualizar notificacion");
    }
}
