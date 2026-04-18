package com.arka.inventory.application.port.out.directory;

import reactor.core.publisher.Mono;

public interface OrganizationDirectoryPort {

    Mono<Boolean> organizationExists(String organizationId);
}
