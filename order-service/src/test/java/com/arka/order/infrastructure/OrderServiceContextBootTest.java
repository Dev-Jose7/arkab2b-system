package com.arka.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.arka.order.OrderServiceApplication;
import com.arka.order.application.service.OrderApplicationService;
import com.arka.order.application.service.OutboxEventRelayPublisher;
import com.arka.order.infrastructure.adapter.in.web.controller.OrderController;
import com.arka.order.infrastructure.adapter.out.event.KafkaDomainEventPublisherAdapter;
import com.arka.order.infrastructure.adapter.out.external.CatalogVariantHttpAdapter;
import com.arka.order.infrastructure.adapter.out.external.DirectoryCheckoutHttpAdapter;
import com.arka.order.infrastructure.adapter.out.external.InventoryReservationHttpAdapter;
import com.arka.order.infrastructure.adapter.out.persistence.CartR2dbcAdapter;
import com.arka.order.infrastructure.adapter.out.persistence.DomainCartRepositoryAdapter;
import com.arka.order.infrastructure.adapter.out.persistence.DomainOrderRepositoryAdapter;
import com.arka.order.infrastructure.adapter.out.persistence.PurchaseOrderR2dbcAdapter;
import com.arka.order.infrastructure.config.OutboxRelayScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = OrderServiceApplication.class,
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
class OrderServiceContextBootTest {


    @Autowired
    private ApplicationContext context;

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private PurchaseOrderR2dbcAdapter purchaseOrderR2dbcAdapter;

    @Autowired
    private CartR2dbcAdapter cartR2dbcAdapter;

    @Autowired
    private DomainOrderRepositoryAdapter domainOrderRepositoryAdapter;

    @Autowired
    private DomainCartRepositoryAdapter domainCartRepositoryAdapter;

    @Autowired
    private CatalogVariantHttpAdapter catalogVariantHttpAdapter;

    @Autowired
    private DirectoryCheckoutHttpAdapter directoryCheckoutHttpAdapter;

    @Autowired
    private InventoryReservationHttpAdapter inventoryReservationHttpAdapter;

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
        assertThat(orderController).isNotNull();
        assertThat(orderApplicationService).isNotNull();
        assertThat(purchaseOrderR2dbcAdapter).isNotNull();
        assertThat(cartR2dbcAdapter).isNotNull();
        assertThat(domainOrderRepositoryAdapter).isNotNull();
        assertThat(domainCartRepositoryAdapter).isNotNull();
        assertThat(catalogVariantHttpAdapter).isNotNull();
        assertThat(directoryCheckoutHttpAdapter).isNotNull();
        assertThat(inventoryReservationHttpAdapter).isNotNull();
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
