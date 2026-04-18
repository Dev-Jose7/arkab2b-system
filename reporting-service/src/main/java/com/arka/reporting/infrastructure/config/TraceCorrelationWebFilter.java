package com.arka.reporting.infrastructure.config;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TraceCorrelationWebFilter implements WebFilter {

    private static final String TRACE_CONTEXT_KEY = "arka.traceId";
    private static final String CORRELATION_CONTEXT_KEY = "arka.correlationId";

    private final String traceHeader;
    private final String correlationHeader;

    public TraceCorrelationWebFilter(
            @Value("${app.observability.trace-id-header:X-Trace-Id}") String traceHeader,
            @Value("${app.observability.correlation-id-header:X-Correlation-Id}") String correlationHeader) {
        this.traceHeader = normalizeOrDefault(traceHeader, "X-Trace-Id");
        this.correlationHeader = normalizeOrDefault(correlationHeader, "X-Correlation-Id");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = resolveTraceId(exchange.getRequest());
        String correlationId = resolveCorrelationId(exchange.getRequest(), traceId);

        ServerHttpRequest decoratedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    if (!headers.containsKey(traceHeader)) {
                        headers.set(traceHeader, traceId);
                    }
                    if (!headers.containsKey(correlationHeader)) {
                        headers.set(correlationHeader, correlationId);
                    }
                })
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(decoratedRequest).build();
        mutatedExchange.getResponse().getHeaders().set(traceHeader, traceId);
        mutatedExchange.getResponse().getHeaders().set(correlationHeader, correlationId);

        return chain.filter(mutatedExchange)
                .contextWrite(context -> context
                        .put(TRACE_CONTEXT_KEY, traceId)
                        .put(CORRELATION_CONTEXT_KEY, correlationId));
    }

    private String resolveTraceId(ServerHttpRequest request) {
        String headerTrace = firstNonBlank(request.getHeaders().getFirst(traceHeader), parseW3cTraceParent(request));
        if (headerTrace != null) {
            return headerTrace;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveCorrelationId(ServerHttpRequest request, String traceId) {
        String fromHeader = firstNonBlank(
                request.getHeaders().getFirst(correlationHeader),
                request.getHeaders().getFirst("X-Request-Id"),
                request.getHeaders().getFirst("request-id"));
        if (fromHeader != null) {
            return fromHeader;
        }
        return traceId;
    }

    private String parseW3cTraceParent(ServerHttpRequest request) {
        String traceParent = request.getHeaders().getFirst("traceparent");
        if (traceParent == null || traceParent.isBlank()) {
            return null;
        }
        String[] parts = traceParent.trim().split("-");
        if (parts.length < 4) {
            return null;
        }
        String traceId = normalize(parts[1]);
        return traceId.isBlank() ? null : traceId;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeOrDefault(String value, String fallback) {
        String normalized = normalize(value);
        return normalized.isBlank() ? fallback : normalized;
    }
}
