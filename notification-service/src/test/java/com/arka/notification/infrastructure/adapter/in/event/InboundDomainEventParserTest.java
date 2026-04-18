package com.arka.notification.infrastructure.adapter.in.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class InboundDomainEventParserTest {

    private final InboundDomainEventParser parser = new InboundDomainEventParser(new ObjectMapper().findAndRegisterModules());

    @Test
    void shouldParseEnvelopeDataPayload() {
        String payload = """
                {
                  "eventId":"evt-100",
                  "eventType":"OrderCreatedFromValidatedCart",
                  "traceId":"trace-100",
                  "correlationId":"corr-100",
                  "data":{
                    "aggregateType":"Order",
                    "aggregateId":"ord-100",
                    "organizationId":"organization-100",
                    "actorId":"actor-100"
                  }
                }
                """;

        InboundDomainEventParser.ParsedInboundDomainEvent parsed = parser.parse(payload);

        assertEquals("evt-100", parsed.eventId());
        assertEquals("OrderCreatedFromValidatedCart", parsed.eventType());
        assertEquals("Order", parsed.aggregateType());
        assertEquals("ord-100", parsed.aggregateId());
        assertEquals("organization-100", parsed.organizationId());
        assertEquals("actor-100", parsed.actorId());
        assertEquals("trace-100", parsed.traceId());
        assertEquals("corr-100", parsed.correlationId());
    }

    @Test
    void shouldGenerateEventIdWhenMissing() {
        String payload = """
                {
                  "eventType":"OrderOperationalStatusUpdated",
                  "data":{"aggregateType":"Order","aggregateId":"ord-200","organizationId":"organization-200"}
                }
                """;

        InboundDomainEventParser.ParsedInboundDomainEvent parsed = parser.parse(payload);

        assertNotNull(parsed.eventId());
        assertEquals("OrderOperationalStatusUpdated", parsed.eventType());
    }
}
