package com.arka.catalog.infrastructure.config;

import com.arka.catalog.infrastructure.adapter.out.security.ServiceToServiceTokenProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class DiscoveryWebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryWebClientConfig.class);
    private static final String TRACE_CONTEXT_KEY = "arka.traceId";
    private static final String CORRELATION_CONTEXT_KEY = "arka.correlationId";

    private final String traceHeader;
    private final String correlationHeader;

    public DiscoveryWebClientConfig(
            @Value("${app.observability.trace-id-header:X-Trace-Id}") String traceHeader,
            @Value("${app.observability.correlation-id-header:X-Correlation-Id}") String correlationHeader) {
        this.traceHeader = normalizeOrDefault(traceHeader, "X-Trace-Id");
        this.correlationHeader = normalizeOrDefault(correlationHeader, "X-Correlation-Id");
    }

    @Bean("loadBalancedNoAuthWebClientBuilder")
    @LoadBalanced
    public WebClient.Builder loadBalancedNoAuthWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean("loadBalancedWebClientBuilder")
    @Primary
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder(
            ServiceToServiceTokenProvider tokenProvider,
            ObjectProvider<MeterRegistry> meterRegistryProvider) {
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        return WebClient.builder()
                .filter(tracePropagationFilter())
                .filter(outboundObservationFilter(meterRegistry))
                .filter(bearerTokenFilter(tokenProvider));
    }

    private ExchangeFilterFunction bearerTokenFilter(ServiceToServiceTokenProvider tokenProvider) {
        return (request, next) -> {
            if (request.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
                return next.exchange(request);
            }
            return tokenProvider.currentToken()
                    .map(token -> ClientRequest.from(request)
                            .headers(headers -> headers.setBearerAuth(token))
                            .build())
                    .flatMap(next::exchange);
        };
    }

    private ExchangeFilterFunction tracePropagationFilter() {
        return (request, next) -> Mono.deferContextual(contextView -> {
            String traceId = firstNonBlank(
                    contextView.getOrDefault(TRACE_CONTEXT_KEY, ""),
                    request.headers().getFirst(traceHeader));
            String correlationId = firstNonBlank(
                    contextView.getOrDefault(CORRELATION_CONTEXT_KEY, ""),
                    request.headers().getFirst(correlationHeader),
                    traceId);

            ClientRequest.Builder builder = ClientRequest.from(request);
            if (traceId != null && !traceId.isBlank() && request.headers().getFirst(traceHeader) == null) {
                builder.headers(headers -> headers.set(traceHeader, traceId));
            }
            if (correlationId != null
                    && !correlationId.isBlank()
                    && request.headers().getFirst(correlationHeader) == null) {
                builder.headers(headers -> headers.set(correlationHeader, correlationId));
            }
            return next.exchange(builder.build());
        });
    }

    private ExchangeFilterFunction outboundObservationFilter(MeterRegistry meterRegistry) {
        return (request, next) -> {
            long startedAt = System.nanoTime();
            String target = Optional.ofNullable(request.url().getHost()).orElse("unknown");
            String method = request.method().name();
            String traceId = request.headers().getFirst(traceHeader);
            String correlationId = request.headers().getFirst(correlationHeader);

            return next.exchange(request)
                    .doOnNext(response -> {
                        String status = Integer.toString(response.statusCode().value());
                        recordClientMetric(meterRegistry, method, target, status, startedAt);
                        if (response.statusCode().isError()) {
                            log.warn(
                                    "Outbound HTTP call returned error status. method={} target={} status={} traceId={} correlationId={}",
                                    method,
                                    target,
                                    status,
                                    normalize(traceId),
                                    normalize(correlationId));
                        }
                    })
                    .doOnError(error -> {
                        recordClientMetric(meterRegistry, method, target, "IO_ERROR", startedAt);
                        log.error(
                                "Outbound HTTP call failed. method={} target={} traceId={} correlationId={} error={}",
                                method,
                                target,
                                normalize(traceId),
                                normalize(correlationId),
                                error.toString());
                    });
        };
    }

    private void recordClientMetric(
            MeterRegistry meterRegistry,
            String method,
            String target,
            String status,
            long startedAtNanos) {
        if (meterRegistry == null) {
            return;
        }
        Timer.builder("arka.http.client.requests")
                .tag("method", normalizeOrDefault(method, "UNKNOWN"))
                .tag("target", normalizeOrDefault(target, "unknown"))
                .tag("status", normalizeOrDefault(status, "UNKNOWN"))
                .register(meterRegistry)
                .record(Duration.ofNanos(Math.max(0L, System.nanoTime() - startedAtNanos)));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeOrDefault(String value, String fallback) {
        String normalized = normalize(value);
        return normalized.isBlank() ? fallback : normalized;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String normalized = value.trim();
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return null;
    }
}
