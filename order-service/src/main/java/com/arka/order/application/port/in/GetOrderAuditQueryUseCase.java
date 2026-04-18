package com.arka.order.application.port.in;

import com.arka.order.application.query.GetOrderAuditQuery;
import com.arka.order.application.result.OrderAuditResult;
import reactor.core.publisher.Mono;

public interface GetOrderAuditQueryUseCase {

    Mono<OrderAuditResult> handle(GetOrderAuditQuery query);
}
