package com.arka.order.application.port.out.audit;

import com.arka.order.application.result.OrderAuditEntryResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderAuditPort {

    Mono<Void> record(
            String organizationId,

            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String outcome,
            String payload);

    Flux<OrderAuditEntryResult> findByOrder(String organizationId, String orderId, int limit);
}
