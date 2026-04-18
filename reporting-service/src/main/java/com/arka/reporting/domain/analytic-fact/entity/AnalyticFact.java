package com.arka.reporting.domain.analyticfact.entity;

import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactStatus;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactType;
import com.arka.reporting.domain.analyticfact.exception.FactAlreadyAppliedException;
import com.arka.reporting.domain.analyticfact.exception.InvalidAnalyticFactTransitionException;
import com.arka.reporting.domain.analyticfact.valueobject.FactId;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.analyticfact.valueobject.TenantId;
import com.arka.reporting.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public final class AnalyticFact {

    private final FactId factId;
    private final TenantId tenantId;
    private final SourceEventId sourceEventId;
    private final String eventType;
    private final Instant occurredAt;
    private final Instant createdAt;

    private AnalyticFactType factType;
    private AnalyticFactStatus factStatus;
    private String rawPayload;
    private String normalizedPayload;
    private String rejectionReason;
    private Instant updatedAt;

    private AnalyticFact(
            FactId factId,
            TenantId tenantId,
            SourceEventId sourceEventId,
            String eventType,
            AnalyticFactType factType,
            String rawPayload,
            String normalizedPayload,
            AnalyticFactStatus factStatus,
            String rejectionReason,
            Instant occurredAt,
            Instant createdAt,
            Instant updatedAt) {
        this.factId = factId;
        this.tenantId = tenantId;
        this.sourceEventId = sourceEventId;
        this.eventType = required(eventType, "eventType");
        this.factType = factType == null ? AnalyticFactType.GENERIC : factType;
        this.rawPayload = rawPayload == null ? "{}" : rawPayload.trim();
        this.normalizedPayload = normalizedPayload == null ? "" : normalizedPayload.trim();
        this.factStatus = factStatus == null ? AnalyticFactStatus.CAPTURED : factStatus;
        this.rejectionReason = rejectionReason == null ? "" : rejectionReason.trim();
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AnalyticFact capture(
            TenantId tenantId,
            SourceEventId sourceEventId,
            String eventType,
            AnalyticFactType factType,
            String rawPayload,
            Instant occurredAt,
            Instant now) {
        return new AnalyticFact(
                FactId.newId(),
                tenantId,
                sourceEventId,
                eventType,
                factType,
                rawPayload,
                "",
                AnalyticFactStatus.CAPTURED,
                "",
                occurredAt,
                now,
                now);
    }

    public static AnalyticFact rehydrate(
            FactId factId,
            TenantId tenantId,
            SourceEventId sourceEventId,
            String eventType,
            AnalyticFactType factType,
            String rawPayload,
            String normalizedPayload,
            AnalyticFactStatus factStatus,
            String rejectionReason,
            Instant occurredAt,
            Instant createdAt,
            Instant updatedAt) {
        return new AnalyticFact(
                factId,
                tenantId,
                sourceEventId,
                eventType,
                factType,
                rawPayload,
                normalizedPayload,
                factStatus,
                rejectionReason,
                occurredAt,
                createdAt,
                updatedAt);
    }

    public void normalize(String normalizedPayload, Instant now) {
        if (factStatus == AnalyticFactStatus.APPLIED || factStatus == AnalyticFactStatus.REJECTED) {
            throw new InvalidAnalyticFactTransitionException(factStatus.name(), AnalyticFactStatus.NORMALIZED.name());
        }
        if (normalizedPayload == null || normalizedPayload.isBlank()) {
            throw new DomainInvariantViolationException("payload_normalizado_requerido", "normalizedPayload es obligatorio");
        }
        this.normalizedPayload = normalizedPayload.trim();
        this.factStatus = AnalyticFactStatus.NORMALIZED;
        this.rejectionReason = "";
        this.updatedAt = now;
    }

    public void apply(Instant now) {
        if (factStatus == AnalyticFactStatus.APPLIED) {
            throw new FactAlreadyAppliedException();
        }
        if (factStatus == AnalyticFactStatus.REJECTED) {
            throw new InvalidAnalyticFactTransitionException(factStatus.name(), AnalyticFactStatus.APPLIED.name());
        }
        if (normalizedPayload == null || normalizedPayload.isBlank()) {
            throw new DomainInvariantViolationException(
                    "payload_normalizado_requerido",
                    "No se puede aplicar un hecho sin payload normalizado");
        }
        this.factStatus = AnalyticFactStatus.APPLIED;
        this.updatedAt = now;
    }

    public void reject(String reason, Instant now) {
        if (factStatus == AnalyticFactStatus.APPLIED) {
            throw new InvalidAnalyticFactTransitionException(factStatus.name(), AnalyticFactStatus.REJECTED.name());
        }
        this.factStatus = AnalyticFactStatus.REJECTED;
        this.rejectionReason = reason == null ? "" : reason.trim();
        this.updatedAt = now;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("analytic_fact_invalido", field + " es obligatorio");
        }
        return value.trim();
    }

    public FactId factId() {
        return factId;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public SourceEventId sourceEventId() {
        return sourceEventId;
    }

    public String eventType() {
        return eventType;
    }

    public AnalyticFactType factType() {
        return factType;
    }

    public String rawPayload() {
        return rawPayload;
    }

    public String normalizedPayload() {
        return normalizedPayload;
    }

    public AnalyticFactStatus factStatus() {
        return factStatus;
    }

    public String rejectionReason() {
        return rejectionReason;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
