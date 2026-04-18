package com.arka.order.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arka.order.infrastructure.adapter.in.web.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class WebExceptionHandlerConfigTest {

    private final WebExceptionHandlerConfig handler = new WebExceptionHandlerConfig();

    @Test
    void shouldPreserveNotFoundStatusForResponseStatusException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleResponseStatus(new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("order_resource_not_found", response.getBody().code());
        assertEquals("order not found", response.getBody().message());
    }

    @Test
    void shouldPreserveUnauthorizedStatusForResponseStatusException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleResponseStatus(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing token"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("order_unauthorized", response.getBody().code());
        assertEquals("missing token", response.getBody().message());
    }
}
