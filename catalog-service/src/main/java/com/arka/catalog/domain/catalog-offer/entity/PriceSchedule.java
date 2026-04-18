package com.arka.catalog.domain.catalogoffer.entity;

import com.arka.catalog.domain.catalogoffer.enumtype.PriceScheduleJobStatus;
import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record PriceSchedule(
        String scheduleId,
        String tenantId,
        String priceId,
        Instant executeAfter,
        PriceScheduleJobStatus jobStatus,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt) {

    public PriceSchedule {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new DomainInvariantViolationException("price_schedule_invalido", "scheduleId es obligatorio");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new DomainInvariantViolationException("tenant_requerido", "tenantId es obligatorio");
        }
        if (priceId == null || priceId.isBlank()) {
            throw new DomainInvariantViolationException("price_schedule_invalido", "priceId es obligatorio");
        }
        if (executeAfter == null) {
            throw new DomainInvariantViolationException("price_schedule_invalido", "executeAfter es obligatorio");
        }
        if (jobStatus == null) {
            throw new DomainInvariantViolationException("price_schedule_invalido", "jobStatus es obligatorio");
        }
    }
}
