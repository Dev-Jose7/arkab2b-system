package com.arka.order.application.port.in;

import com.arka.order.application.query.ListOrderPaymentsQuery;
import com.arka.order.application.result.ManualPaymentResult;
import reactor.core.publisher.Flux;

public interface ListOrderPaymentsQueryUseCase {

    Flux<ManualPaymentResult> handle(ListOrderPaymentsQuery query);
}
