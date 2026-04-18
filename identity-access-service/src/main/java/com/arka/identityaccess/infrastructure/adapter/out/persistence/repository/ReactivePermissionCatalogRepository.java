package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.PermissionCatalogRow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactivePermissionCatalogRepository extends ReactiveCrudRepository<PermissionCatalogRow, String> {

    Mono<PermissionCatalogRow> findByPermissionCode(String permissionCode);

    Mono<Boolean> existsByPermissionCode(String permissionCode);
}
