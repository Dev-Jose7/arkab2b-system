package com.arka.reporting.domain.analyticfact.aggregate;

import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.event.AnalyticFactApplied;
import com.arka.reporting.domain.shared.event.DomainEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class AnalyticFactAggregate {

    private final AnalyticFact fact;
    private final List<DomainEvent> domainEvents;

    private AnalyticFactAggregate(AnalyticFact fact, List<DomainEvent> domainEvents) {
        this.fact = fact;
        this.domainEvents = domainEvents;
    }

    public static AnalyticFactAggregate rehydrate(AnalyticFact fact) {
        return new AnalyticFactAggregate(fact, new ArrayList<>());
    }

    public void normalize(String normalizedPayload, Instant now) {
        fact.normalize(normalizedPayload, now);
    }

    public void apply(Instant now) {
        fact.apply(now);
        domainEvents.add(new AnalyticFactApplied(
                fact.factId().value(),
                fact.organizationId().value(),
                fact.sourceEventId().value(),
                fact.factType().name(),
                now));
    }

    public void reject(String reason, Instant now) {
        fact.reject(reason, now);
    }

    public AnalyticFact fact() {
        return fact;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
