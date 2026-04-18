package com.arka.notification.infrastructure.adapter.in.web.mapper.query;

import com.arka.notification.application.query.GetNotificationAuditQuery;
import com.arka.notification.application.query.GetNotificationByIdQuery;
import com.arka.notification.application.query.GetNotificationDetailQuery;
import com.arka.notification.application.query.GetNotificationMetricsQuery;
import com.arka.notification.application.query.GetNotificationTimelineQuery;
import com.arka.notification.application.query.ListNotificationAttemptsQuery;
import com.arka.notification.application.query.SearchNotificationsQuery;
import com.arka.notification.infrastructure.adapter.in.security.IamSecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
public class NotificationQueryMapper {

    public GetNotificationByIdQuery toGetByIdQuery(String notificationId, IamSecurityPrincipal principal) {
        return new GetNotificationByIdQuery(principal.tenantId(), notificationId);
    }

    public GetNotificationDetailQuery toGetDetailQuery(String notificationId, IamSecurityPrincipal principal) {
        return new GetNotificationDetailQuery(principal.tenantId(), notificationId);
    }

    public ListNotificationAttemptsQuery toListAttemptsQuery(String notificationId, IamSecurityPrincipal principal) {
        return new ListNotificationAttemptsQuery(principal.tenantId(), notificationId);
    }

    public GetNotificationTimelineQuery toTimelineQuery(String notificationId, IamSecurityPrincipal principal) {
        return new GetNotificationTimelineQuery(principal.tenantId(), notificationId);
    }

    public SearchNotificationsQuery toSearchQuery(
            String status,
            String sourceEventType,
            String channel,
            String recipientRef,
            Integer page,
            Integer size,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new SearchNotificationsQuery(
                principal.tenantId(),
                status,
                sourceEventType,
                channel,
                recipientRef,
                safePage,
                safeSize);
    }

    public GetNotificationMetricsQuery toMetricsQuery(IamSecurityPrincipal principal) {
        return new GetNotificationMetricsQuery(principal.tenantId());
    }

    public GetNotificationAuditQuery toAuditQuery(
            String targetType,
            String targetId,
            Integer page,
            Integer size,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new GetNotificationAuditQuery(principal.tenantId(), targetType, targetId, safePage, safeSize);
    }
}
