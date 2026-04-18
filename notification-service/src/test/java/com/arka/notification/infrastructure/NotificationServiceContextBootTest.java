package com.arka.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.arka.notification.NotificationServiceApplication;
import com.arka.notification.application.service.NotificationApplicationService;
import com.arka.notification.application.service.OutboxEventRelayPublisher;
import com.arka.notification.infrastructure.adapter.in.event.OrderDomainEventKafkaConsumer;
import com.arka.notification.infrastructure.adapter.in.web.controller.NotificationController;
import com.arka.notification.infrastructure.adapter.out.event.KafkaDomainEventPublisherAdapter;
import com.arka.notification.infrastructure.adapter.out.external.DirectoryRecipientResolverHttpAdapter;
import com.arka.notification.infrastructure.adapter.out.external.NotificationProviderHttpAdapter;
import com.arka.notification.infrastructure.adapter.out.persistence.DomainNotificationDispatchRepositoryAdapter;
import com.arka.notification.infrastructure.adapter.out.persistence.NotificationR2dbcPersistenceAdapter;
import com.arka.notification.infrastructure.config.OutboxRelayScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = NotificationServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.r2dbc.url=r2dbc:postgresql://localhost:5432/arkab2b_test",
            "spring.r2dbc.username=arka",
            "spring.r2dbc.password=arka",
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.listener.auto-startup=false",
                "app.kafka.consumers.order-events.enabled=false",
                "spring.task.scheduling.enabled=false",
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
            "app.database.schema.initialize-on-startup=false"
        })
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class NotificationServiceContextBootTest {


    @Autowired
    private ApplicationContext context;

    @Autowired
    private NotificationController notificationController;

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private NotificationR2dbcPersistenceAdapter notificationR2dbcPersistenceAdapter;

    @Autowired
    private DomainNotificationDispatchRepositoryAdapter domainNotificationDispatchRepositoryAdapter;

    @Autowired
    private NotificationProviderHttpAdapter notificationProviderHttpAdapter;

    @Autowired
    private DirectoryRecipientResolverHttpAdapter directoryRecipientResolverHttpAdapter;

    @Autowired
    private KafkaDomainEventPublisherAdapter kafkaDomainEventPublisherAdapter;

    @Autowired
    private OutboxEventRelayPublisher outboxEventRelayPublisher;

    @Autowired
    private OutboxRelayScheduler outboxRelayScheduler;

    @Autowired
    private OrderDomainEventKafkaConsumer orderDomainEventKafkaConsumer;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextShouldLoadWithCriticalWiring() {
        assertThat(context).isNotNull();
        assertThat(notificationController).isNotNull();
        assertThat(notificationApplicationService).isNotNull();
        assertThat(notificationR2dbcPersistenceAdapter).isNotNull();
        assertThat(domainNotificationDispatchRepositoryAdapter).isNotNull();
        assertThat(notificationProviderHttpAdapter).isNotNull();
        assertThat(directoryRecipientResolverHttpAdapter).isNotNull();
        assertThat(kafkaDomainEventPublisherAdapter).isNotNull();
        assertThat(outboxEventRelayPublisher).isNotNull();
        assertThat(outboxRelayScheduler).isNotNull();
        assertThat(orderDomainEventKafkaConsumer).isNotNull();
        assertThat(context.getBeanNamesForType(SecurityWebFilterChain.class)).isNotEmpty();
    }

    @Test
    void actuatorHealthEndpointsShouldBeAvailable() {
        assertHealthEndpoint("/actuator/health");
        assertHealthEndpoint("/actuator/health/liveness");
        assertHealthEndpoint("/actuator/health/readiness");
    }

    private void assertHealthEndpoint(String uri) {
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .value(status -> assertThat(status).isIn(200, 503))
                .expectBody()
                .jsonPath("$.status")
                .exists();
    }
}
