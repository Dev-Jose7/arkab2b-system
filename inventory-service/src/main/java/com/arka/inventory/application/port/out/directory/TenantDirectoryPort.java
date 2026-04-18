package com.arka.inventory.application.port.out.directory;

import reactor.core.publisher.Mono;

public interface TenantDirectoryPort {

    Mono<Boolean> tenantExists(String tenantId);
}
