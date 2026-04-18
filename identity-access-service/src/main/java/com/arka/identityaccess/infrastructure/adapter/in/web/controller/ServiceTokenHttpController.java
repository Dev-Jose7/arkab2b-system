package com.arka.identityaccess.infrastructure.adapter.in.web.controller;

import com.arka.identityaccess.infrastructure.adapter.in.security.ServiceTokenIssuerService;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.ServiceTokenIssueRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.ServiceTokenResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/internal/auth")
public class ServiceTokenHttpController {

    private final ServiceTokenIssuerService serviceTokenIssuerService;

    public ServiceTokenHttpController(ServiceTokenIssuerService serviceTokenIssuerService) {
        this.serviceTokenIssuerService = serviceTokenIssuerService;
    }

    @PostMapping("/service-token")
    public Mono<ServiceTokenResponse> issueServiceToken(@Valid @RequestBody ServiceTokenIssueRequest request) {
        return serviceTokenIssuerService.issue(request);
    }
}
