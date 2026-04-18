package com.arka.notification.infrastructure.adapter.in.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InboundDomainEventParser {

    private static final Logger log = LoggerFactory.getLogger(InboundDomainEventParser.class);

    private final ObjectMapper objectMapper;

    public InboundDomainEventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedInboundDomainEvent parse(String rawPayload) {
        try {
            JsonNode root = objectMapper.readTree(rawPayload == null ? "{}" : rawPayload);
            JsonNode data = root.has("data") && root.get("data").isObject() ? root.get("data") : root;

            String eventId = firstNonBlank(root, data, "eventId", "event_id", "id");
            String eventType = firstNonBlank(root, data, "eventType", "event_type", "mutationType", "mutation_type", "type");
            String aggregateType = firstNonBlank(root, data, "aggregateType", "aggregate_type", "targetType", "target_type");
            String aggregateId = firstNonBlank(root, data, "aggregateId", "aggregate_id", "targetId", "target_id");
            String organizationId = firstNonBlank(root, data, "organizationId", "organization_id");
            String actorId = firstNonBlank(root, data, "actorId", "actor_id", "actorUserId", "actor_user_id", "userId", "user_id");
            String traceId = firstNonBlank(root, data, "traceId", "trace_id");
            String correlationId = firstNonBlank(root, data, "correlationId", "correlation_id");
            Instant occurredAt = parseInstant(firstNonBlank(root, data, "occurredAt", "occurred_at", "timestamp"));

            if (eventId == null || eventId.isBlank()) {
                String seed = (rawPayload == null ? "" : rawPayload) + "::" + (eventType == null ? "" : eventType);
                eventId = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
            }

            organizationId = firstNonBlankLiteral(organizationId, organizationId, firstNonBlank(root, data, "recipientRef"));

            String payloadJson = objectMapper.writeValueAsString(data);
            return new ParsedInboundDomainEvent(
                    eventId,
                    eventType,
                    aggregateType,
                    aggregateId,
                    organizationId,
                    actorId,
                    traceId,
                    correlationId,
                    occurredAt,
                    payloadJson,
                    rawPayload);
        } catch (Exception exception) {
            log.error("Failed to parse inbound domain event payload", exception);
            throw new IllegalArgumentException("invalid inbound event payload", exception);
        }
    }

    private String firstNonBlank(JsonNode first, JsonNode second, String... fields) {
        String value = firstNonBlank(first, fields);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return firstNonBlank(second, fields);
    }

    private String firstNonBlank(JsonNode node, String... fields) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        for (String field : fields) {
            JsonNode candidate = node.get(field);
            if (candidate == null || candidate.isNull()) {
                continue;
            }
            String value = candidate.asText("").trim();
            if (!value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlankLiteral(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    public record ParsedInboundDomainEvent(
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            String organizationId,

            String actorId,
            String traceId,
            String correlationId,
            Instant occurredAt,
            String payloadJson,
            String rawPayload) {
    }
}
