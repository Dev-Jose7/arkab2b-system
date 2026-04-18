package com.arka.notification.infrastructure.adapter.out.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class KafkaDomainEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;

    @Test
    void shouldPublishEventWhenKafkaIsAvailable() {
        when(kafkaTemplateProvider.getIfAvailable()).thenReturn(kafkaTemplate);
        when(kafkaTemplate.send(eq("topic-a"), eq("key-a"), eq("{\"ok\":true}")))
                .thenReturn(CompletableFuture.completedFuture(null));

        KafkaDomainEventPublisherAdapter adapter =
                new KafkaDomainEventPublisherAdapter(kafkaTemplateProvider, true);

        StepVerifier.create(adapter.publish("topic-a", "key-a", "{\"ok\":true}"))
                .verifyComplete();

        verify(kafkaTemplate).send(eq("topic-a"), eq("key-a"), eq("{\"ok\":true}"));
    }

    @Test
    void shouldFailWhenKafkaIsUnavailable() {
        when(kafkaTemplateProvider.getIfAvailable()).thenReturn(null);

        KafkaDomainEventPublisherAdapter adapter =
                new KafkaDomainEventPublisherAdapter(kafkaTemplateProvider, true);

        StepVerifier.create(adapter.publish("topic-a", "key-a", "{\"ok\":true}"))
                .expectError(IllegalStateException.class)
                .verify();
    }

}
