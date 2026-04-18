package com.arka.reporting.infrastructure.adapter.out.event;

import com.arka.reporting.application.port.out.event.DomainEventTopicPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StaticDomainEventTopicAdapter implements DomainEventTopicPort {

    private final String weeklySalesTopic;
    private final String weeklyReplenishmentTopic;
    private final String analyticFactAppliedTopic;
    private final String reportingMutationTopic;

    public StaticDomainEventTopicAdapter(
            @Value("${app.kafka.topics.weekly-sales-report-generated:reporting.weekly-sales-report-generated.v1}")
                    String weeklySalesTopic,
            @Value("${app.kafka.topics.weekly-replenishment-report-generated:reporting.weekly-replenishment-report-generated.v1}")
                    String weeklyReplenishmentTopic,
            @Value("${app.kafka.topics.analytic-fact-applied:reporting.analytic-fact-applied.v1}")
                    String analyticFactAppliedTopic,
            @Value("${app.kafka.topics.reporting-mutation:reporting.mutation.v1}")
                    String reportingMutationTopic) {
        this.weeklySalesTopic = weeklySalesTopic;
        this.weeklyReplenishmentTopic = weeklyReplenishmentTopic;
        this.analyticFactAppliedTopic = analyticFactAppliedTopic;
        this.reportingMutationTopic = reportingMutationTopic;
    }

    @Override
    public String topicFor(String eventType) {
        return switch (eventType) {
            case "WeeklySalesReportGenerated" -> weeklySalesTopic;
            case "WeeklyReplenishmentReportGenerated" -> weeklyReplenishmentTopic;
            case "AnalyticFactApplied" -> analyticFactAppliedTopic;
            default -> reportingMutationTopic;
        };
    }
}
