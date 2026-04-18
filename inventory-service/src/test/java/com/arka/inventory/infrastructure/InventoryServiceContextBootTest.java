package com.arka.inventory.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.arka.inventory.InventoryServiceApplication;
import com.arka.inventory.application.service.InventoryApplicationService;
import com.arka.inventory.application.service.OutboxEventRelayPublisher;
import com.arka.inventory.infrastructure.adapter.in.web.controller.InventoryController;
import com.arka.inventory.infrastructure.adapter.out.event.KafkaDomainEventPublisherAdapter;
import com.arka.inventory.infrastructure.adapter.out.external.CatalogSkuHttpAdapter;
import com.arka.inventory.infrastructure.adapter.out.external.DirectoryTenantHttpAdapter;
import com.arka.inventory.infrastructure.adapter.out.external.OrderReferenceHttpAdapter;
import com.arka.inventory.infrastructure.adapter.out.persistence.DomainInventoryBalanceRepositoryAdapter;
import com.arka.inventory.infrastructure.adapter.out.persistence.InventoryAuditR2dbcAdapter;
import com.arka.inventory.infrastructure.adapter.out.persistence.StockItemR2dbcAdapter;
import com.arka.inventory.infrastructure.config.OutboxRelayScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = InventoryServiceApplication.class,
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
class InventoryServiceContextBootTest {


    @Autowired
    private ApplicationContext context;

    @Autowired
    private InventoryController inventoryController;

    @Autowired
    private InventoryApplicationService inventoryApplicationService;

    @Autowired
    private DomainInventoryBalanceRepositoryAdapter domainInventoryBalanceRepositoryAdapter;

    @Autowired
    private StockItemR2dbcAdapter stockItemR2dbcAdapter;

    @Autowired
    private InventoryAuditR2dbcAdapter inventoryAuditR2dbcAdapter;

    @Autowired
    private CatalogSkuHttpAdapter catalogSkuHttpAdapter;

    @Autowired
    private OrderReferenceHttpAdapter orderReferenceHttpAdapter;

    @Autowired
    private DirectoryTenantHttpAdapter directoryTenantHttpAdapter;

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
        assertThat(inventoryController).isNotNull();
        assertThat(inventoryApplicationService).isNotNull();
        assertThat(domainInventoryBalanceRepositoryAdapter).isNotNull();
        assertThat(stockItemR2dbcAdapter).isNotNull();
        assertThat(inventoryAuditR2dbcAdapter).isNotNull();
        assertThat(catalogSkuHttpAdapter).isNotNull();
        assertThat(orderReferenceHttpAdapter).isNotNull();
        assertThat(directoryTenantHttpAdapter).isNotNull();
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
