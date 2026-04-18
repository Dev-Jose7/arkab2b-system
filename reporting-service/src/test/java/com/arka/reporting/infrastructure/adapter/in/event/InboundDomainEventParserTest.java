package com.arka.reporting.infrastructure.adapter.in.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class InboundDomainEventParserTest {

    private final InboundDomainEventParser parser = new InboundDomainEventParser(new ObjectMapper().findAndRegisterModules());

    @Test
    void shouldParseEventEnvelopeAndDataObject() {
        String payload = """
                {
                  "eventId":"evt-r-1",
                  "eventType":"InventoryStockAdjusted",
                  "traceId":"trace-r-1",
                  "correlationId":"corr-r-1",
                  "data":{
                    "aggregateType":"InventoryBalance",
                    "aggregateId":"bal-1",
                    "organizationId":"organization-r-1"
                  }
                }
                """;

        InboundDomainEventParser.ParsedInboundDomainEvent parsed = parser.parse(payload);

        assertEquals("evt-r-1", parsed.eventId());
        assertEquals("InventoryStockAdjusted", parsed.eventType());
        assertEquals("InventoryBalance", parsed.aggregateType());
        assertEquals("bal-1", parsed.aggregateId());
        assertEquals("organization-r-1", parsed.organizationId());
        assertEquals("trace-r-1", parsed.traceId());
        assertEquals("corr-r-1", parsed.correlationId());
    }

    @Test
    void shouldGenerateDeterministicIdWhenMissing() {
        String payload = """
                {
                  "eventType":"CatalogOfferPublished",
                  "data":{"aggregateType":"CatalogOffer","aggregateId":"offer-1","organizationId":"organization-r-2"}
                }
                """;

        InboundDomainEventParser.ParsedInboundDomainEvent parsed = parser.parse(payload);

        assertNotNull(parsed.eventId());
        assertEquals("CatalogOfferPublished", parsed.eventType());
    }
}
