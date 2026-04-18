package com.arka.directory.application.port.out.audit;

import com.arka.directory.application.result.DirectoryAuditEntryResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DirectoryAuditPort {

    Mono<Void> record(
            String organizationId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String outcome,
            String payload);

    Flux<DirectoryAuditEntryResult> findByOrganization(String organizationId, int limit);

    Mono<Long> countByOrganization(String organizationId);
}
