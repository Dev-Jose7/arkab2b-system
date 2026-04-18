package com.arka.directory.domain.countrypolicy.aggregate;

import com.arka.directory.domain.countrypolicy.enumtype.CountryPolicyStatus;
import com.arka.directory.domain.countrypolicy.enumtype.WeekStartsOn;
import com.arka.directory.domain.countrypolicy.event.RegionalPolicyAppliedInOperation;
import com.arka.directory.domain.countrypolicy.event.RegionalPolicyConfigured;
import com.arka.directory.domain.organizationcontext.valueobject.CountryCode;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.domain.organizationcontext.valueobject.PolicyVersion;
import com.arka.directory.domain.organizationcontext.valueobject.RegionalPolicyResolution;
import com.arka.directory.domain.shared.event.DomainEvent;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class CountryPolicy {

    private final String policyId;
    private final OrganizationId organizationId;
    private final CountryCode countryCode;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents;

    private PolicyVersion policyVersion;
    private String currencyCode;
    private WeekStartsOn weekStartsOn;
    private String weeklyCutoffLocalTime;
    private String timezone;
    private int reportingRetentionDays;
    private boolean requiresVerifiedAddress;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private CountryPolicyStatus status;
    private Instant updatedAt;

    private CountryPolicy(
            String policyId,
            OrganizationId organizationId,
            CountryCode countryCode,
            PolicyVersion policyVersion,
            String currencyCode,
            WeekStartsOn weekStartsOn,
            String weeklyCutoffLocalTime,
            String timezone,
            int reportingRetentionDays,
            boolean requiresVerifiedAddress,
            Instant effectiveFrom,
            Instant effectiveTo,
            CountryPolicyStatus status,
            Instant createdAt,
            Instant updatedAt,
            List<DomainEvent> domainEvents) {
        this.policyId = requireNotBlank(policyId, "policyId");
        this.organizationId = organizationId;
        this.countryCode = countryCode;
        this.policyVersion = policyVersion;
        this.currencyCode = requireNotBlank(currencyCode, "currencyCode").toUpperCase();
        this.weekStartsOn = weekStartsOn;
        this.weeklyCutoffLocalTime = requireNotBlank(weeklyCutoffLocalTime, "weeklyCutoffLocalTime");
        this.timezone = requireNotBlank(timezone, "timezone");
        this.reportingRetentionDays = reportingRetentionDays;
        if (this.reportingRetentionDays <= 0) {
            throw new DomainInvariantViolationException("reportingRetentionDays must be positive");
        }
        this.requiresVerifiedAddress = requiresVerifiedAddress;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.domainEvents = domainEvents == null ? new ArrayList<>() : domainEvents;
    }

    public static CountryPolicy configure(
            String policyId,
            OrganizationId organizationId,
            CountryCode countryCode,
            PolicyVersion previousVersion,
            String currencyCode,
            WeekStartsOn weekStartsOn,
            String weeklyCutoffLocalTime,
            String timezone,
            int reportingRetentionDays,
            boolean requiresVerifiedAddress,
            Instant effectiveFrom,
            Instant now) {
        PolicyVersion nextVersion = previousVersion == null ? PolicyVersion.of(1) : previousVersion.next();

        CountryPolicy policy = new CountryPolicy(
                policyId,
                organizationId,
                countryCode,
                nextVersion,
                currencyCode,
                weekStartsOn,
                weeklyCutoffLocalTime,
                timezone,
                reportingRetentionDays,
                requiresVerifiedAddress,
                effectiveFrom == null ? now : effectiveFrom,
                null,
                CountryPolicyStatus.ACTIVE,
                now,
                now,
                new ArrayList<>());

        policy.domainEvents.add(new RegionalPolicyConfigured(
                now,
                organizationId.value(),
                countryCode.value(),
                policy.policyVersion.value()));

        return policy;
    }

    public static CountryPolicy rehydrate(
            String policyId,
            OrganizationId organizationId,
            CountryCode countryCode,
            PolicyVersion policyVersion,
            String currencyCode,
            WeekStartsOn weekStartsOn,
            String weeklyCutoffLocalTime,
            String timezone,
            int reportingRetentionDays,
            boolean requiresVerifiedAddress,
            Instant effectiveFrom,
            Instant effectiveTo,
            CountryPolicyStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new CountryPolicy(
                policyId,
                organizationId,
                countryCode,
                policyVersion,
                currencyCode,
                weekStartsOn,
                weeklyCutoffLocalTime,
                timezone,
                reportingRetentionDays,
                requiresVerifiedAddress,
                effectiveFrom,
                effectiveTo,
                status,
                createdAt,
                updatedAt,
                new ArrayList<>());
    }

    public void supersede(Instant now) {
        if (!status.isActive()) {
            return;
        }
        this.status = CountryPolicyStatus.SUPERSEDED;
        this.effectiveTo = now;
        this.updatedAt = now;
    }

    public RegionalPolicyResolution applyToOperation(String operationCode, String actorUserId, Instant now) {
        if (!status.isActive()) {
            throw new DomainInvariantViolationException("Only active policy can be applied to operations");
        }
        if (operationCode == null || operationCode.isBlank()) {
            throw new DomainInvariantViolationException("operationCode is required");
        }
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new DomainInvariantViolationException("actorUserId is required");
        }

        Instant eventTime = now == null ? Instant.now() : now;
        domainEvents.add(new RegionalPolicyAppliedInOperation(
                eventTime,
                organizationId.value(),
                countryCode.value(),
                policyVersion.value(),
                operationCode.trim(),
                actorUserId.trim()));

        return new RegionalPolicyResolution(
                organizationId.value(),
                countryCode.value(),
                policyVersion.value(),
                currencyCode,
                timezone,
                weeklyCutoffLocalTime,
                reportingRetentionDays,
                requiresVerifiedAddress,
                operationCode.trim(),
                actorUserId.trim());
    }

    public String policyId() {
        return policyId;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public CountryCode countryCode() {
        return countryCode;
    }

    public PolicyVersion policyVersion() {
        return policyVersion;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public WeekStartsOn weekStartsOn() {
        return weekStartsOn;
    }

    public String weeklyCutoffLocalTime() {
        return weeklyCutoffLocalTime;
    }

    public String timezone() {
        return timezone;
    }

    public int reportingRetentionDays() {
        return reportingRetentionDays;
    }

    public boolean requiresVerifiedAddress() {
        return requiresVerifiedAddress;
    }

    public Instant effectiveFrom() {
        return effectiveFrom;
    }

    public Instant effectiveTo() {
        return effectiveTo;
    }

    public CountryPolicyStatus status() {
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
}
