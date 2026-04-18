package com.arka.notification.infrastructure.adapter.out.external;

import com.arka.notification.application.port.out.external.TemplateRendererPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SimpleTemplateRendererAdapter implements TemplateRendererPort {

    @Override
    public Mono<String> render(String subjectTemplate, String bodyTemplate, String payloadJson) {
        String safeSubject = subjectTemplate == null ? "" : subjectTemplate.trim();
        String safeBody = bodyTemplate == null ? "" : bodyTemplate.trim();
        String safePayload = payloadJson == null || payloadJson.isBlank() ? "{}" : payloadJson.trim();
        String rendered = "{"
                + "\"subject\":\"" + escapeJson(safeSubject) + "\"," 
                + "\"body\":\"" + escapeJson(safeBody) + "\"," 
                + "\"data\":" + safePayload
                + "}";
        return Mono.just(rendered);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
