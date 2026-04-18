package com.arka.identityaccess.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.arka.identityaccess.IdentityAccessServiceApplication;
import com.arka.identityaccess.application.service.OutboxEventRelayPublisher;
import com.arka.identityaccess.application.usecase.command.LoginUseCase;
import com.arka.identityaccess.infrastructure.adapter.in.security.JwtReactiveAuthenticationManager;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.AdminIamHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.AuthHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.JwksHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.SessionAdminHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.TokenIntrospectController;
import com.arka.identityaccess.infrastructure.adapter.out.event.KafkaDomainEventPublisherAdapter;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.DomainAccountRepositoryAdapter;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.DomainSessionRepositoryAdapter;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.UserR2dbcRepositoryAdapter;
import com.arka.identityaccess.infrastructure.adapter.out.security.BCryptPasswordHasherAdapter;
import com.arka.identityaccess.infrastructure.adapter.out.security.JwtSignerAdapter;
import com.arka.identityaccess.infrastructure.adapter.out.security.JwtVerificationAdapter;
import com.arka.identityaccess.infrastructure.adapter.out.security.TokenIssuerAdapter;
import com.arka.identityaccess.infrastructure.config.OutboxRelayScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        classes = IdentityAccessServiceApplication.class,
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
class IdentityAccessServiceContextBootTest {


    @Autowired
    private ApplicationContext context;

    @Autowired
    private AuthHttpController authHttpController;

    @Autowired
    private AdminIamHttpController adminIamHttpController;

    @Autowired
    private SessionAdminHttpController sessionAdminHttpController;

    @Autowired
    private TokenIntrospectController tokenIntrospectController;

    @Autowired
    private JwksHttpController jwksHttpController;

    @Autowired
    private LoginUseCase loginUseCase;

    @Autowired
    private UserR2dbcRepositoryAdapter userR2dbcRepositoryAdapter;

    @Autowired
    private DomainAccountRepositoryAdapter domainAccountRepositoryAdapter;

    @Autowired
    private DomainSessionRepositoryAdapter domainSessionRepositoryAdapter;

    @Autowired
    private TokenIssuerAdapter tokenIssuerAdapter;

    @Autowired
    private BCryptPasswordHasherAdapter bCryptPasswordHasherAdapter;

    @Autowired
    private JwtSignerAdapter jwtSignerAdapter;

    @Autowired
    private JwtVerificationAdapter jwtVerificationAdapter;

    @Autowired
    private JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager;

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
        assertThat(authHttpController).isNotNull();
        assertThat(adminIamHttpController).isNotNull();
        assertThat(sessionAdminHttpController).isNotNull();
        assertThat(tokenIntrospectController).isNotNull();
        assertThat(jwksHttpController).isNotNull();
        assertThat(loginUseCase).isNotNull();
        assertThat(userR2dbcRepositoryAdapter).isNotNull();
        assertThat(domainAccountRepositoryAdapter).isNotNull();
        assertThat(domainSessionRepositoryAdapter).isNotNull();
        assertThat(tokenIssuerAdapter).isNotNull();
        assertThat(bCryptPasswordHasherAdapter).isNotNull();
        assertThat(jwtSignerAdapter).isNotNull();
        assertThat(jwtVerificationAdapter).isNotNull();
        assertThat(jwtReactiveAuthenticationManager).isNotNull();
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
