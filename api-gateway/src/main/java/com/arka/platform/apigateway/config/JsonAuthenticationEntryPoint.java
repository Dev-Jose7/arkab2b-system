package com.arka.platform.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JsonAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final JsonAccessDeniedHandler payloadWriter;

    public JsonAuthenticationEntryPoint(JsonAccessDeniedHandler payloadWriter) {
        this.payloadWriter = payloadWriter;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
        return payloadWriter.write(exchange, HttpStatus.UNAUTHORIZED, "unauthorized", "Authentication is required");
    }
}
