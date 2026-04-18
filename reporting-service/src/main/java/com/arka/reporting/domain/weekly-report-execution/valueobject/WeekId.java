package com.arka.reporting.domain.weeklyreportexecution.valueobject;

import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;

public record WeekId(String value) {

    public WeekId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("week_id_invalido", "weekId es obligatorio");
        }
        if (!value.matches("\\d{4}-W\\d{2}")) {
            throw new DomainInvariantViolationException("week_id_invalido", "weekId debe usar formato YYYY-Www");
        }
        value = value.trim();
    }

    public static WeekId of(String value) {
        return new WeekId(value);
    }
}
