package com.arka.notification.infrastructure.adapter.in.web.controller;

import com.arka.notification.application.port.in.DiscardNotificationCommandUseCase;
import com.arka.notification.application.port.in.DispatchNotificationCommandUseCase;
import com.arka.notification.application.port.in.EmitRelevantChangeNotificationCommandUseCase;
import com.arka.notification.application.port.in.GetNotificationAuditQueryUseCase;
import com.arka.notification.application.port.in.GetNotificationByIdQueryUseCase;
import com.arka.notification.application.port.in.GetNotificationDetailQueryUseCase;
import com.arka.notification.application.port.in.GetNotificationMetricsQueryUseCase;
import com.arka.notification.application.port.in.GetNotificationTimelineQueryUseCase;
import com.arka.notification.application.port.in.ListNotificationAttemptsQueryUseCase;
import com.arka.notification.application.port.in.ProcessProviderCallbackCommandUseCase;
import com.arka.notification.application.port.in.RecordNotificationDeliveryCommandUseCase;
import com.arka.notification.application.port.in.ReprocessNotificationDlqCommandUseCase;
import com.arka.notification.application.port.in.RetryNotificationCommandUseCase;
import com.arka.notification.application.port.in.SearchNotificationsQueryUseCase;
import com.arka.notification.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.notification.infrastructure.adapter.in.web.mapper.command.NotificationCommandMapper;
import com.arka.notification.infrastructure.adapter.in.web.mapper.query.NotificationQueryMapper;
import com.arka.notification.infrastructure.adapter.in.web.mapper.response.NotificationResponseMapper;
import com.arka.notification.infrastructure.adapter.in.web.request.DiscardNotificationRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.DispatchNotificationRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.EmitNotificationRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.ProcessProviderCallbackRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.RecordNotificationDeliveryRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.ReprocessNotificationDlqRequest;
import com.arka.notification.infrastructure.adapter.in.web.request.RetryNotificationRequest;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationAttemptResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationAuditResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationDetailResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationMetricsResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationSearchResponse;
import com.arka.notification.infrastructure.adapter.in.web.response.NotificationTimelineResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationCommandMapper commandMapper;
    private final NotificationQueryMapper queryMapper;
    private final NotificationResponseMapper responseMapper;
    private final EmitRelevantChangeNotificationCommandUseCase emitRelevantChangeNotificationCommandUseCase;
    private final DispatchNotificationCommandUseCase dispatchNotificationCommandUseCase;
    private final RetryNotificationCommandUseCase retryNotificationCommandUseCase;
    private final DiscardNotificationCommandUseCase discardNotificationCommandUseCase;
    private final RecordNotificationDeliveryCommandUseCase recordNotificationDeliveryCommandUseCase;
    private final ProcessProviderCallbackCommandUseCase processProviderCallbackCommandUseCase;
    private final ReprocessNotificationDlqCommandUseCase reprocessNotificationDlqCommandUseCase;
    private final GetNotificationByIdQueryUseCase getNotificationByIdQueryUseCase;
    private final SearchNotificationsQueryUseCase searchNotificationsQueryUseCase;
    private final GetNotificationDetailQueryUseCase getNotificationDetailQueryUseCase;
    private final ListNotificationAttemptsQueryUseCase listNotificationAttemptsQueryUseCase;
    private final GetNotificationTimelineQueryUseCase getNotificationTimelineQueryUseCase;
    private final GetNotificationMetricsQueryUseCase getNotificationMetricsQueryUseCase;
    private final GetNotificationAuditQueryUseCase getNotificationAuditQueryUseCase;

    public NotificationController(
            NotificationCommandMapper commandMapper,
            NotificationQueryMapper queryMapper,
            NotificationResponseMapper responseMapper,
            EmitRelevantChangeNotificationCommandUseCase emitRelevantChangeNotificationCommandUseCase,
            DispatchNotificationCommandUseCase dispatchNotificationCommandUseCase,
            RetryNotificationCommandUseCase retryNotificationCommandUseCase,
            DiscardNotificationCommandUseCase discardNotificationCommandUseCase,
            RecordNotificationDeliveryCommandUseCase recordNotificationDeliveryCommandUseCase,
            ProcessProviderCallbackCommandUseCase processProviderCallbackCommandUseCase,
            ReprocessNotificationDlqCommandUseCase reprocessNotificationDlqCommandUseCase,
            GetNotificationByIdQueryUseCase getNotificationByIdQueryUseCase,
            SearchNotificationsQueryUseCase searchNotificationsQueryUseCase,
            GetNotificationDetailQueryUseCase getNotificationDetailQueryUseCase,
            ListNotificationAttemptsQueryUseCase listNotificationAttemptsQueryUseCase,
            GetNotificationTimelineQueryUseCase getNotificationTimelineQueryUseCase,
            GetNotificationMetricsQueryUseCase getNotificationMetricsQueryUseCase,
            GetNotificationAuditQueryUseCase getNotificationAuditQueryUseCase) {
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.responseMapper = responseMapper;
        this.emitRelevantChangeNotificationCommandUseCase = emitRelevantChangeNotificationCommandUseCase;
        this.dispatchNotificationCommandUseCase = dispatchNotificationCommandUseCase;
        this.retryNotificationCommandUseCase = retryNotificationCommandUseCase;
        this.discardNotificationCommandUseCase = discardNotificationCommandUseCase;
        this.recordNotificationDeliveryCommandUseCase = recordNotificationDeliveryCommandUseCase;
        this.processProviderCallbackCommandUseCase = processProviderCallbackCommandUseCase;
        this.reprocessNotificationDlqCommandUseCase = reprocessNotificationDlqCommandUseCase;
        this.getNotificationByIdQueryUseCase = getNotificationByIdQueryUseCase;
        this.searchNotificationsQueryUseCase = searchNotificationsQueryUseCase;
        this.getNotificationDetailQueryUseCase = getNotificationDetailQueryUseCase;
        this.listNotificationAttemptsQueryUseCase = listNotificationAttemptsQueryUseCase;
        this.getNotificationTimelineQueryUseCase = getNotificationTimelineQueryUseCase;
        this.getNotificationMetricsQueryUseCase = getNotificationMetricsQueryUseCase;
        this.getNotificationAuditQueryUseCase = getNotificationAuditQueryUseCase;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @PostMapping
    public Mono<NotificationResponse> emitNotification(
            @Valid @RequestBody EmitNotificationRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return emitRelevantChangeNotificationCommandUseCase
                .handle(commandMapper.toCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/{notificationId}")
    public Mono<NotificationResponse> getById(
            @PathVariable String notificationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getNotificationByIdQueryUseCase
                .handle(queryMapper.toGetByIdQuery(notificationId, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/{notificationId}/detail")
    public Mono<NotificationDetailResponse> getDetail(
            @PathVariable String notificationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getNotificationDetailQueryUseCase
                .handle(queryMapper.toGetDetailQuery(notificationId, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping
    public Mono<NotificationSearchResponse> search(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sourceEventType", required = false) String sourceEventType,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestParam(name = "recipientRef", required = false) String recipientRef,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return searchNotificationsQueryUseCase
                .handle(queryMapper.toSearchQuery(status, sourceEventType, channel, recipientRef, page, size, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @PostMapping("/{notificationId}/dispatch")
    public Mono<NotificationDetailResponse> dispatch(
            @PathVariable String notificationId,
            @RequestBody(required = false) DispatchNotificationRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return dispatchNotificationCommandUseCase
                .handle(commandMapper.toCommand(notificationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @PostMapping("/{notificationId}/retry")
    public Mono<NotificationDetailResponse> retry(
            @PathVariable String notificationId,
            @RequestBody(required = false) RetryNotificationRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return retryNotificationCommandUseCase
                .handle(commandMapper.toCommand(notificationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @PostMapping("/{notificationId}/discard")
    public Mono<NotificationResponse> discard(
            @PathVariable String notificationId,
            @Valid @RequestBody DiscardNotificationRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return discardNotificationCommandUseCase
                .handle(commandMapper.toCommand(notificationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @PostMapping("/{notificationId}/deliveries")
    public Mono<NotificationResponse> recordDelivery(
            @PathVariable String notificationId,
            @Valid @RequestBody RecordNotificationDeliveryRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return recordNotificationDeliveryCommandUseCase
                .handle(commandMapper.toCommand(notificationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/{notificationId}/attempts")
    public Flux<NotificationAttemptResponse> listAttempts(
            @PathVariable String notificationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listNotificationAttemptsQueryUseCase
                .handle(queryMapper.toListAttemptsQuery(notificationId, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/{notificationId}/timeline")
    public Mono<NotificationTimelineResponse> getTimeline(
            @PathVariable String notificationId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getNotificationTimelineQueryUseCase
                .handle(queryMapper.toTimelineQuery(notificationId, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @PostMapping("/provider-callbacks")
    public Mono<NotificationDetailResponse> processProviderCallback(
            @Valid @RequestBody ProcessProviderCallbackRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return processProviderCallbackCommandUseCase
                .handle(commandMapper.toCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @PostMapping("/{notificationId}/reprocess-dlq")
    public Mono<NotificationDetailResponse> reprocessDlq(
            @PathVariable String notificationId,
            @Valid @RequestBody ReprocessNotificationDlqRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return reprocessNotificationDlqCommandUseCase
                .handle(commandMapper.toCommand(notificationId, request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @GetMapping("/metrics")
    public Mono<NotificationMetricsResponse> metrics(Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getNotificationMetricsQueryUseCase
                .handle(queryMapper.toMetricsQuery(principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_NOTIFICATION_ADMIN','ROLE_TRUSTED_SERVICE','ROLE_ARKA_ADMIN')")
    @GetMapping("/audits")
    public Mono<NotificationAuditResponse> audits(
            @RequestParam(name = "targetType", required = false) String targetType,
            @RequestParam(name = "targetId", required = false) String targetId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getNotificationAuditQueryUseCase
                .handle(queryMapper.toAuditQuery(targetType, targetId, page, size, principal))
                .map(responseMapper::toResponse);
    }
}
