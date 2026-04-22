package com.arka.directory.domain.organizationcontext.aggregate;

import com.arka.directory.domain.organizationcontext.enumtype.OrganizationStatus;
import com.arka.directory.domain.organizationcontext.event.OrganizationRegistered;
import com.arka.directory.domain.organizationcontext.event.OrganizationStatusChanged;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Organization {

    private final OrganizationId id;
    private final CountryCode countryCode;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents;

    private String legalName;
    private String tradeName;
    private String currencyCode;
    private String timezone;
    private String segmentTier;
    private OrganizationStatus status;
    private Instant updatedAt;

    private Organization(
            OrganizationId id,
            String legalName,
            String tradeName,
            CountryCode countryCode,
            String currencyCode,
            String timezone,
            String segmentTier,
            OrganizationStatus status,
            Instant createdAt,
            Instant updatedAt,
            List<DomainEvent> domainEvents) {
        this.id = id;
        this.legalName = requireNotBlank(legalName, "legalName");
        this.tradeName = normalizeOptional(tradeName);
        this.countryCode = countryCode;
        this.currencyCode = requireNotBlank(currencyCode, "currencyCode").toUpperCase();
        this.timezone = requireNotBlank(timezone, "timezone");
        this.segmentTier = normalizeOptional(segmentTier);
        this.status = status == null ? OrganizationStatus.ONBOARDING : status;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        this.domainEvents = domainEvents == null ? new ArrayList<>() : domainEvents;
    }

    public static Organization register(
            OrganizationId organizationId,
            String legalName,
            String tradeName,
            CountryCode countryCode,
            String currencyCode,
            String timezone,
            String segmentTier,
            Instant now) {
        Organization organization = new Organization(
                organizationId,
                legalName,
                tradeName,
                countryCode,
                currencyCode,
                timezone,
                segmentTier,
                OrganizationStatus.ONBOARDING,
                now,
                now,
                new ArrayList<>());

        organization.domainEvents.add(new OrganizationRegistered(
                now,
                organization.id.value(),
                organization.countryCode.value()));
        return organization;
    }

    public static Organization rehydrate(
            OrganizationId organizationId,
            String legalName,
            String tradeName,
            CountryCode countryCode,
            String currencyCode,
            String timezone,
            String segmentTier,
            OrganizationStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new Organization(
                organizationId,
                legalName,
                tradeName,
                countryCode,
                currencyCode,
                timezone,
                segmentTier,
                status,
                createdAt,
                updatedAt,
                new ArrayList<>());
    }

    public void transitionStatus(OrganizationStatus newStatus, Instant now) {
        if (newStatus == null) {
            throw new DomainInvariantViolationException("Organization status is required");
        }
        OrganizationStatus previous = this.status;
        if (previous == newStatus) {
            return;
        }

        if (previous == OrganizationStatus.INACTIVE && newStatus != OrganizationStatus.INACTIVE) {
            throw new DomainInvariantViolationException("Inactive organization cannot transition to another status");
        }

        this.status = newStatus;
        this.updatedAt = now == null ? Instant.now() : now;
        this.domainEvents.add(new OrganizationStatusChanged(
                this.updatedAt,
                this.id.value(),
                previous.name(),
                this.status.name()));
    }

    public void assertOwnership(OrganizationId expectedOrganizationId) {
        if (expectedOrganizationId == null || !id.value().equals(expectedOrganizationId.value())) {
            throw new DomainInvariantViolationException("Operation organization does not match aggregate organization");
        }
    }

    public void ensureActiveForSensitiveOperation() {
        if (!status.isOperational()) {
            throw new DomainInvariantViolationException("Organization must be ACTIVE for sensitive operations");
        }
    }

    public void ensureAllowsMutations() {
        if (!status.allowsProfileMutations()) {
            throw new DomainInvariantViolationException("Organization state does not allow profile mutations");
        }
    }

    public OrganizationId id() {
        return id;
    }

    public String legalName() {
        return legalName;
    }

    public String tradeName() {
        return tradeName;
    }

    public CountryCode countryCode() {
        return countryCode;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public String timezone() {
        return timezone;
    }

    public String segmentTier() {
        return segmentTier;
    }

    public OrganizationStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> pulled = List.copyOf(domainEvents);
        domainEvents.clear();
        return pulled;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
