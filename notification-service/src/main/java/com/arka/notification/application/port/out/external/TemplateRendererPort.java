package com.arka.notification.application.port.out.external;

import reactor.core.publisher.Mono;

public interface TemplateRendererPort {

    Mono<String> render(String subjectTemplate, String bodyTemplate, String payloadJson);
}
