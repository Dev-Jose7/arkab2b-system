package com.arka.identityaccess.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.infrastructure.adapter.in.security.ServiceTokenIssuerService;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.ServiceTokenHttpController;
import com.arka.identityaccess.infrastructure.config.ServiceClientRegistryProperties;
import com.arka.identityaccess.infrastructure.config.WebExceptionHandlerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(
        controllers = ServiceTokenHttpController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
@Import({
        ServiceTokenIssuerService.class,
        ServiceClientRegistryProperties.class,
        WebExceptionHandlerConfig.class
})
@TestPropertySource(properties = {
        "app.security.jwt.issuer=identity-access-service",
        "app.security.s2s.enabled=true",
        "app.security.s2s.default-token-ttl-seconds=300",
        "app.security.s2s.clients.order-service.client-secret=order-service-secret",
        "app.security.s2s.clients.order-service.scopes[0]=order.read",
        "app.security.s2s.clients.order-service.audiences[0]=arka-b2b"
})
class ServiceTokenHttpControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtSigningPort jwtSigningPort;

    @Test
    void shouldReturnUnauthorizedWhenServiceClientSecretIsInvalid() {
        webTestClient.post()
                .uri("/api/v1/internal/auth/service-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "clientId": "order-service",
                          "clientSecret": "wrong-secret",
                          "audience": "arka-b2b",
                          "scopes": ["order.read"]
                        }
                        """)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("unauthorized")
                .jsonPath("$.message").isEqualTo("invalid service client credentials");
    }

    @Test
    void shouldReturnForbiddenWhenRequestedScopeIsNotAllowed() {
        webTestClient.post()
                .uri("/api/v1/internal/auth/service-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "clientId": "order-service",
                          "clientSecret": "order-service-secret",
                          "audience": "arka-b2b",
                          "scopes": ["admin.root"]
                        }
                        """)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("access_denied")
                .jsonPath("$.message").isEqualTo("requested scopes are not allowed");
    }

    @Test
    void shouldIssueServiceTokenWhenRequestIsValid() {
        when(jwtSigningPort.signServiceToken(any())).thenReturn(Mono.just("signed-service-token"));

        webTestClient.post()
                .uri("/api/v1/internal/auth/service-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "clientId": "order-service",
                          "clientSecret": "order-service-secret",
                          "audience": "arka-b2b",
                          "scopes": ["order.read"]
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("signed-service-token")
                .jsonPath("$.scope").isEqualTo("order.read")
                .jsonPath("$.clientId").isEqualTo("order-service");
    }
}
