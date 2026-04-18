package com.arka.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.arka.catalog.CatalogServiceApplication;
import com.arka.catalog.application.service.CatalogApplicationService;
import com.arka.catalog.application.service.OutboxEventRelayPublisher;
import com.arka.catalog.infrastructure.adapter.in.web.controller.CatalogHttpController;
import com.arka.catalog.infrastructure.adapter.out.event.KafkaDomainEventPublisherAdapter;
import com.arka.catalog.infrastructure.adapter.out.external.DirectoryRegionalPolicyContextHttpAdapter;
import com.arka.catalog.infrastructure.adapter.out.external.IdentityActorLegitimacyHttpAdapter;
import com.arka.catalog.infrastructure.adapter.out.persistence.CatalogR2dbcPersistenceAdapter;
import com.arka.catalog.infrastructure.adapter.out.persistence.DomainCatalogOfferRepositoryAdapter;
import com.arka.catalog.infrastructure.config.OutboxRelayScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = CatalogServiceApplication.class,
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
class CatalogServiceContextBootTest {


    @Autowired
    private ApplicationContext context;

    @Autowired
    private CatalogHttpController catalogHttpController;

    @Autowired
    private CatalogApplicationService catalogApplicationService;

    @Autowired
    private CatalogR2dbcPersistenceAdapter catalogR2dbcPersistenceAdapter;

    @Autowired
    private DomainCatalogOfferRepositoryAdapter domainCatalogOfferRepositoryAdapter;

    @Autowired
    private IdentityActorLegitimacyHttpAdapter identityActorLegitimacyHttpAdapter;

    @Autowired
    private DirectoryRegionalPolicyContextHttpAdapter directoryRegionalPolicyContextHttpAdapter;

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
        assertThat(catalogHttpController).isNotNull();
        assertThat(catalogApplicationService).isNotNull();
        assertThat(catalogR2dbcPersistenceAdapter).isNotNull();
        assertThat(domainCatalogOfferRepositoryAdapter).isNotNull();
        assertThat(identityActorLegitimacyHttpAdapter).isNotNull();
        assertThat(directoryRegionalPolicyContextHttpAdapter).isNotNull();
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
