package com.arka.platform.apigateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(
        classes = ApiGatewayApplication.class,
        properties = {
            "spring.cloud.config.enabled=false",
            "spring.cloud.discovery.enabled=false",
            "eureka.client.enabled=false",
            "management.health.discovery.enabled=false",
            "management.health.discoveryComposite.enabled=false"
        })
class ApiGatewayContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
