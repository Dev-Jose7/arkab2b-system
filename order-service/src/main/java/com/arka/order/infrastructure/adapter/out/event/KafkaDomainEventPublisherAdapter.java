package com.arka.order.infrastructure.adapter.out.event;

import com.arka.order.application.port.out.event.DomainEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisherAdapter.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final boolean kafkaEnabled;

    public KafkaDomainEventPublisherAdapter(
            ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider,
            @Value("${app.dependencies.kafka.enabled:true}") boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        this.kafkaEnabled = kafkaEnabled;
    }

    @Override
    public Mono<Void> publish(String topic, String key, String payload) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            log.error("Kafka deshabilitado o no configurado para publicar eventos de dominio. topic={} key={}", topic, key);
            return Mono.error(new IllegalStateException(
                    "Kafka deshabilitado o no configurado para publicar eventos de dominio"));
        }
        return Mono.fromFuture(kafkaTemplate.send(topic, key, payload)).then();
    }
}
