package com.arka.catalog.infrastructure.adapter.out.event;

import com.arka.catalog.application.port.out.event.DomainEventTopicPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StaticDomainEventTopicAdapter implements DomainEventTopicPort {

    private final String catalogOfferPublishedTopic;
    private final String catalogOfferUpdatedTopic;
    private final String catalogMutationTopic;

    public StaticDomainEventTopicAdapter(
            @Value("${app.kafka.topics.catalog-offer-published:catalog.offer-published.v1}") String catalogOfferPublishedTopic,
            @Value("${app.kafka.topics.catalog-offer-updated:catalog.offer-updated.v1}") String catalogOfferUpdatedTopic,
            @Value("${app.kafka.topics.catalog-mutation:catalog.mutation.v1}") String catalogMutationTopic) {
        this.catalogOfferPublishedTopic = catalogOfferPublishedTopic;
        this.catalogOfferUpdatedTopic = catalogOfferUpdatedTopic;
        this.catalogMutationTopic = catalogMutationTopic;
    }

    @Override
    public String topicFor(String eventType) {
        if ("CatalogOfferPublished".equals(eventType)) {
            return catalogOfferPublishedTopic;
        }
        if ("CatalogOfferUpdated".equals(eventType)) {
            return catalogOfferUpdatedTopic;
        }
        return catalogMutationTopic;
    }
}
