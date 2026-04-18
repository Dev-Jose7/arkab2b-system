package com.arka.directory.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.arka.directory.DirectoryServiceApplication;
import com.arka.directory.application.service.DirectoryApplicationService;
import com.arka.directory.application.service.OutboxEventRelayPublisher;
import com.arka.directory.infrastructure.adapter.in.web.controller.DirectoryController;
import com.arka.directory.infrastructure.adapter.out.event.KafkaDomainEventPublisherAdapter;
import com.arka.directory.infrastructure.adapter.out.persistence.DirectoryOrganizationR2dbcAdapter;
import com.arka.directory.infrastructure.adapter.out.persistence.DomainOrganizationRepositoryAdapter;
import com.arka.directory.infrastructure.config.OutboxRelayScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = DirectoryServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.r2dbc.url=r2dbc:postgresql://localhost:5432/arkab2b_test",
            "spring.r2dbc.username=arka",
            "spring.r2dbc.password=arka",
            "spring.kafka.bootstrap-servers=localhost:9092",
            "spring.kafka.listener.auto-startup=false",
            "spring.task.scheduling.enabled=false",
            "spring.data.redis.host=localhost",
            "spring.data.redis.port=6379",
            "app.database.schema.initialize-on-startup=false"
        })
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class DirectoryServiceContextBootTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DirectoryController directoryController;

    @Autowired
    private DirectoryApplicationService directoryApplicationService;

    @Autowired
    private DirectoryOrganizationR2dbcAdapter directoryOrganizationR2dbcAdapter;

    @Autowired
    private DomainOrganizationRepositoryAdapter domainOrganizationRepositoryAdapter;

    @Autowired
    private KafkaDomainEventPublisherAdapter kafkaDomainEventPublisherAdapter;

    @Autowired
    private OutboxEventRelayPublisher outboxEventRelayPublisher;

    @Autowired
    private OutboxRelayScheduler outboxRelayScheduler;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextShouldLoadWithCriticalWiring() {
        assertThat(context).isNotNull();
        assertThat(directoryController).isNotNull();
        assertThat(directoryApplicationService).isNotNull();
        assertThat(directoryOrganizationR2dbcAdapter).isNotNull();
        assertThat(domainOrganizationRepositoryAdapter).isNotNull();
        assertThat(kafkaDomainEventPublisherAdapter).isNotNull();
        assertThat(outboxEventRelayPublisher).isNotNull();
        assertThat(outboxRelayScheduler).isNotNull();
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
