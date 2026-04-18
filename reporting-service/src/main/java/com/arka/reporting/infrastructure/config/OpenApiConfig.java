package com.arka.reporting.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reportingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Reporting Service API")
                .version("v1")
                .description("Reporting service for analytic ingestion, derived projections and weekly snapshots"));
    }
}
