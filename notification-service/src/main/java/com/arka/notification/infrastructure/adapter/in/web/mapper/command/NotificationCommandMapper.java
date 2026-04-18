package com.arka.notification.infrastructure.adapter.in.web.mapper.command;

import com.arka.notification.application.command.DiscardNotificationCommand;
import com.arka.notification.application.command.DispatchNotificationCommand;
import com.arka.notification.application.command.EmitRelevantChangeNotificationCommand;
import com.arka.notification.application.command.ProcessProviderCallbackCommand;
import com.arka.notification.application.command.RecordNotificationDeliveryCommand;
import com.arka.notification.application.command.ReprocessNotificationDlqCommand;
import com.arka.notification.application.command.RetryNotificationCommand;
import com.arka.notification.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.notification.infrastructure.adapter.in.web.request.DiscardNotificationRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.DispatchNotificationRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.EmitNotificationRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.ProcessProviderCallbackRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.RecordNotificationDeliveryRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.ReprocessNotificationDlqRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.RetryNotificationRequest;
import org.springframework.stereotype.Component;

@Component
public class NotificationCommandMapper {

    public EmitRelevantChangeNotificationCommand toCommand(EmitNotificationRequest request, IamSecurityPrincipal principal) {
        return new EmitRelevantChangeNotificationCommand(
                principal.organizationId(),
                principal.actorId(),
                request.sourceEventId(),
                request.sourceEventType(),
                request.recipientRef(),
                request.channel(),
                request.payloadJson(),
                request.traceId(),
                request.correlationId(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public DispatchNotificationCommand toCommand(
            String notificationId,
            DispatchNotificationRequest request,
            IamSecurityPrincipal principal) {
        return new DispatchNotificationCommand(
                principal.organizationId(),
                principal.actorId(),
                notificationId,
                request == null ? null : normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public RetryNotificationCommand toCommand(
            String notificationId,
            RetryNotificationRequest request,
            IamSecurityPrincipal principal) {
        return new RetryNotificationCommand(
                principal.organizationId(),
                principal.actorId(),
                notificationId,
                request == null ? null : normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public DiscardNotificationCommand toCommand(
            String notificationId,
            DiscardNotificationRequest request,
            IamSecurityPrincipal principal) {
        return new DiscardNotificationCommand(
                principal.organizationId(),
                principal.actorId(),
                notificationId,
                request.reason(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public RecordNotificationDeliveryCommand toCommand(
            String notificationId,
            RecordNotificationDeliveryRequest request,
            IamSecurityPrincipal principal) {
        return new RecordNotificationDeliveryCommand(
                principal.organizationId(),
                principal.actorId(),
                notificationId,
                request.attemptId(),
                request.providerCode(),
                request.providerRef(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public ProcessProviderCallbackCommand toCommand(
            ProcessProviderCallbackRequest request,
            IamSecurityPrincipal principal) {
        return new ProcessProviderCallbackCommand(
                principal.organizationId(),
                principal.actorId(),
                request.notificationId(),
                request.providerCode(),
                request.providerRef(),
                request.callbackEventId(),
                request.callbackStatus(),
                request.payload(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public ReprocessNotificationDlqCommand toCommand(
            String notificationId,
            ReprocessNotificationDlqRequest request,
            IamSecurityPrincipal principal) {
        return new ReprocessNotificationDlqCommand(
                principal.organizationId(),
                principal.actorId(),
                notificationId,
                request.dlqEventId(),
                request.consumerName(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
