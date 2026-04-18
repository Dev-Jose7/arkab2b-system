package com.arka.reporting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.arka.reporting.ReportingServiceApplication;
import com.arka.reporting.application.service.OutboxEventRelayPublisher;
import com.arka.reporting.application.service.ReportingApplicationService;
import com.arka.reporting.infrastructure.adapter.in.event.UpstreamDomainEventKafkaConsumer;
import com.arka.reporting.infrastructure.adapter.in.web.controller.ReportingController;
import com.arka.reporting.infrastructure.adapter.out.event.KafkaDomainEventPublisherAdapter;
import com.arka.reporting.infrastructure.adapter.out.external.FileSystemArtifactStorageAdapter;
import com.arka.reporting.infrastructure.adapter.out.persistence.DomainAnalyticFactRepositoryAdapter;
import com.arka.reporting.infrastructure.adapter.out.persistence.DomainWeeklyReportExecutionRepositoryAdapter;
import com.arka.reporting.infrastructure.adapter.out.persistence.ReportingR2dbcPersistenceAdapter;
import com.arka.reporting.infrastructure.config.OutboxRelayScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = ReportingServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.r2dbc.url=r2dbc:postgresql://localhost:5432/arkab2b_test",
            "spring.r2dbc.username=arka",
            "spring.r2dbc.password=arka",
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.listener.auto-startup=false",
                "app.kafka.consumers.upstream-events.enabled=false",
                "spring.task.scheduling.enabled=false",
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
            "app.database.schema.initialize-on-startup=false"
        })
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReportingServiceContextBootTest {


    @Autowired
    private ApplicationContext context;

    @Autowired
    private ReportingController reportingController;

    @Autowired
    private ReportingApplicationService reportingApplicationService;

    @Autowired
    private ReportingR2dbcPersistenceAdapter reportingR2dbcPersistenceAdapter;

    @Autowired
    private DomainAnalyticFactRepositoryAdapter domainAnalyticFactRepositoryAdapter;

    @Autowired
    private DomainWeeklyReportExecutionRepositoryAdapter domainWeeklyReportExecutionRepositoryAdapter;

    @Autowired
    private FileSystemArtifactStorageAdapter fileSystemArtifactStorageAdapter;

    @Autowired
    private KafkaDomainEventPublisherAdapter kafkaDomainEventPublisherAdapter;

    @Autowired
    private OutboxEventRelayPublisher outboxEventRelayPublisher;

    @Autowired
    private OutboxRelayScheduler outboxRelayScheduler;

    @Autowired
    private UpstreamDomainEventKafkaConsumer upstreamDomainEventKafkaConsumer;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextShouldLoadWithCriticalWiring() {
        assertThat(context).isNotNull();
        assertThat(reportingController).isNotNull();
        assertThat(reportingApplicationService).isNotNull();
        assertThat(reportingR2dbcPersistenceAdapter).isNotNull();
        assertThat(domainAnalyticFactRepositoryAdapter).isNotNull();
        assertThat(domainWeeklyReportExecutionRepositoryAdapter).isNotNull();
        assertThat(fileSystemArtifactStorageAdapter).isNotNull();
        assertThat(kafkaDomainEventPublisherAdapter).isNotNull();
        assertThat(outboxEventRelayPublisher).isNotNull();
        assertThat(outboxRelayScheduler).isNotNull();
        assertThat(upstreamDomainEventKafkaConsumer).isNotNull();
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
