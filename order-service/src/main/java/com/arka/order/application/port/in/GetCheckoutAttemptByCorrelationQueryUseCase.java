package com.arka.order.application.port.in;

import com.arka.order.application.query.GetCheckoutAttemptByCorrelationQuery;
import com.arka.order.application.result.CheckoutAttemptResult;
import reactor.core.publisher.Mono;

public interface GetCheckoutAttemptByCorrelationQueryUseCase {

    Mono<CheckoutAttemptResult> handle(GetCheckoutAttemptByCorrelationQuery query);
}
