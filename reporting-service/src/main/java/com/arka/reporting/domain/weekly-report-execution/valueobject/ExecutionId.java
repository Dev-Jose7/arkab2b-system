package com.arka.reporting.domain.weeklyreportexecution.valueobject;

import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record ExecutionId(String value) {

    public ExecutionId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("execution_id_invalido", "executionId es obligatorio");
        }
        value = value.trim();
    }

    public static ExecutionId of(String value) {
        return new ExecutionId(value);
    }

    public static ExecutionId newId() {
        return new ExecutionId(UUID.randomUUID().toString());
    }
}
