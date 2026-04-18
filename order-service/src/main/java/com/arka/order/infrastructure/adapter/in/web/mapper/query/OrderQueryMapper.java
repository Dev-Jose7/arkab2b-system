package com.arka.order.infrastructure.adapter.in.web.mapper.query;

import com.arka.order.application.query.CalculateOrderAmountsQuery;
import com.arka.order.application.query.GetActiveCartQuery;
import com.arka.order.application.query.GetCartQuery;
import com.arka.order.application.query.GetCheckoutAttemptByCorrelationQuery;
import com.arka.order.application.query.GetOrderAuditQuery;
import com.arka.order.application.query.GetOrderFinancialStatusQuery;
import com.arka.order.application.query.GetOrderQuery;
import com.arka.order.application.query.GetOrderTimelineQuery;
import com.arka.order.application.query.ListOrderPaymentsQuery;
import com.arka.order.application.query.ListOrdersQuery;
import com.arka.order.infrastructure.adapter.in.security.IamSecurityPrincipal;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OrderQueryMapper {

    public GetActiveCartQuery toQuery(IamSecurityPrincipal principal, String userId) {
        String effectiveUser = userId == null || userId.isBlank() ? principal.userId() : userId.trim();
        return new GetActiveCartQuery(
                principal.tenantId(),
                principal.organizationId(),
                effectiveUser,
                principal.userId());
    }

    public GetCartQuery toCartQuery(String cartId, IamSecurityPrincipal principal) {
        return new GetCartQuery(
                principal.tenantId(),
                principal.organizationId(),
                cartId,
                principal.userId());
    }

    public GetCheckoutAttemptByCorrelationQuery toCheckoutAttemptQuery(String checkoutCorrelationId, IamSecurityPrincipal principal) {
        return new GetCheckoutAttemptByCorrelationQuery(
                principal.tenantId(),
                principal.organizationId(),
                checkoutCorrelationId,
                principal.userId());
    }

    public GetOrderQuery toOrderQuery(String orderId, IamSecurityPrincipal principal) {
        return new GetOrderQuery(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                principal.userId());
    }

    public ListOrdersQuery toListOrdersQuery(
            IamSecurityPrincipal principal,
            String status,
            Instant createdFrom,
            Instant createdTo,
            Integer limit) {
        return new ListOrdersQuery(
                principal.tenantId(),
                principal.organizationId(),
                status,
                createdFrom,
                createdTo,
                limit,
                principal.userId());
    }

    public GetOrderTimelineQuery toTimelineQuery(String orderId, IamSecurityPrincipal principal) {
        return new GetOrderTimelineQuery(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                principal.userId());
    }

    public GetOrderFinancialStatusQuery toFinancialStatusQuery(String orderId, IamSecurityPrincipal principal) {
        return new GetOrderFinancialStatusQuery(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                principal.userId());
    }

    public ListOrderPaymentsQuery toListPaymentsQuery(String orderId, IamSecurityPrincipal principal) {
        return new ListOrderPaymentsQuery(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                principal.userId());
    }

    public GetOrderAuditQuery toAuditQuery(String orderId, Integer limit, IamSecurityPrincipal principal) {
        return new GetOrderAuditQuery(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                limit,
                principal.userId());
    }

    public CalculateOrderAmountsQuery toAmountsQuery(String orderId, IamSecurityPrincipal principal) {
        return new CalculateOrderAmountsQuery(
                principal.tenantId(),
                principal.organizationId(),
                orderId,
                principal.userId());
    }
}
