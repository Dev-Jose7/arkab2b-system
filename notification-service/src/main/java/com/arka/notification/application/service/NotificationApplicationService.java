package com.arka.notification.application.service;

import com.arka.notification.application.command.DiscardNotificationCommand;
import com.arka.notification.application.command.DispatchNotificationCommand;
import com.arka.notification.application.command.EmitRelevantChangeNotificationCommand;
import com.arka.notification.application.command.ProcessProviderCallbackCommand;
import com.arka.notification.application.command.RecordNotificationDeliveryCommand;
import com.arka.notification.application.command.ReprocessNotificationDlqCommand;
import com.arka.notification.application.command.RetryNotificationCommand;
import com.arka.notification.application.exception.ActorNotLegitimateException;
import com.arka.notification.application.exception.ApplicationException;
import com.arka.notification.application.exception.IdempotencyConflictException;
import com.arka.notification.application.exception.NotificationResourceNotFoundException;
import com.arka.notification.application.mapper.command.IdempotencySupport;
import com.arka.notification.application.mapper.result.NotificationResultMapper;
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
import com.arka.notification.application.port.out.audit.NotificationAuditEntry;
import com.arka.notification.application.port.out.audit.NotificationAuditPort;
import com.arka.notification.application.port.out.cache.NotificationSearchCachePort;
import com.arka.notification.application.port.out.directory.RecipientResolution;
import com.arka.notification.application.port.out.directory.RecipientResolverPort;
import com.arka.notification.application.port.out.event.DomainEventTopicPort;
import com.arka.notification.application.port.out.external.ActorLegitimacyPort;
import com.arka.notification.application.port.out.external.ClockPort;
import com.arka.notification.application.port.out.external.NotificationProviderPort;
import com.arka.notification.application.port.out.external.ProviderSendRequest;
import com.arka.notification.application.port.out.external.ProviderSendResult;
import com.arka.notification.application.port.out.external.TemplateRendererPort;
import com.arka.notification.application.port.out.persistence.NotificationAttemptPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationMetricsProjection;
import com.arka.notification.application.port.out.persistence.NotificationReadPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationSearchFilter;
import com.arka.notification.application.port.out.persistence.OutboxPersistencePort;
import com.arka.notification.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.notification.application.port.out.persistence.ProviderCallbackPersistencePort;
import com.arka.notification.application.port.out.persistence.ProviderCallbackProjection;
import com.arka.notification.application.port.out.persistence.NotificationTemplatePolicyPersistencePort;
import com.arka.notification.application.port.out.security.ActorContext;
import com.arka.notification.application.port.out.security.ActorContextProviderPort;
import com.arka.notification.application.query.GetNotificationAuditQuery;
import com.arka.notification.application.query.GetNotificationByIdQuery;
import com.arka.notification.application.query.GetNotificationDetailQuery;
import com.arka.notification.application.query.GetNotificationMetricsQuery;
import com.arka.notification.application.query.GetNotificationTimelineQuery;
import com.arka.notification.application.query.ListNotificationAttemptsQuery;
import com.arka.notification.application.query.SearchNotificationsQuery;
import com.arka.notification.application.result.NotificationAttemptResult;
import com.arka.notification.application.result.NotificationAuditResult;
import com.arka.notification.application.result.NotificationDetailResult;
import com.arka.notification.application.result.NotificationMetricsResult;
import com.arka.notification.application.result.NotificationResult;
import com.arka.notification.application.result.NotificationSearchResult;
import com.arka.notification.application.result.NotificationTimelineItemResult;
import com.arka.notification.application.result.NotificationTimelineResult;
import com.arka.notification.application.result.ProviderCallbackResult;
import com.arka.notification.domain.notificationdispatch.aggregate.NotificationDispatch;
import com.arka.notification.domain.notificationdispatch.entity.ChannelPolicy;
import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.entity.NotificationTemplate;
import com.arka.notification.domain.notificationdispatch.entity.ProviderCallback;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationRequestStatus;
import com.arka.notification.domain.notificationdispatch.enumtype.ProviderCallbackStatus;
import com.arka.notification.domain.notificationdispatch.event.NotificationMutationEvent;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.RelevantChangeNotification;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import com.arka.notification.domain.shared.event.DomainEvent;
import com.arka.notification.domain.shared.exception.OperationNotPermittedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NotificationApplicationService
        implements EmitRelevantChangeNotificationCommandUseCase,
                DispatchNotificationCommandUseCase,
                RetryNotificationCommandUseCase,
                DiscardNotificationCommandUseCase,
                RecordNotificationDeliveryCommandUseCase,
                ProcessProviderCallbackCommandUseCase,
                ReprocessNotificationDlqCommandUseCase,
                GetNotificationByIdQueryUseCase,
                SearchNotificationsQueryUseCase,
                GetNotificationDetailQueryUseCase,
                ListNotificationAttemptsQueryUseCase,
                GetNotificationTimelineQueryUseCase,
                GetNotificationMetricsQueryUseCase,
                GetNotificationAuditQueryUseCase {

    private static final String PROCESSED_EVENT_CONSUMER = "notification-service";

    private final NotificationRequestPersistencePort requestPersistencePort;
    private final NotificationAttemptPersistencePort attemptPersistencePort;
    private final NotificationTemplatePolicyPersistencePort templatePolicyPersistencePort;
    private final ProviderCallbackPersistencePort providerCallbackPersistencePort;
    private final NotificationReadPersistencePort readPersistencePort;
    private final ProcessedEventPersistencePort processedEventPersistencePort;
    private final NotificationAuditPort notificationAuditPort;
    private final NotificationSearchCachePort notificationSearchCachePort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final RecipientResolverPort recipientResolverPort;
    private final TemplateRendererPort templateRendererPort;
    private final NotificationProviderPort notificationProviderPort;
    private final DomainEventTopicPort domainEventTopicPort;
    private final ActorContextProviderPort actorContextProviderPort;
    private final ActorLegitimacyPort actorLegitimacyPort;
    private final ClockPort clockPort;
    private final NotificationResultMapper resultMapper;

    public NotificationApplicationService(
            NotificationRequestPersistencePort requestPersistencePort,
            NotificationAttemptPersistencePort attemptPersistencePort,
            NotificationTemplatePolicyPersistencePort templatePolicyPersistencePort,
            ProviderCallbackPersistencePort providerCallbackPersistencePort,
            NotificationReadPersistencePort readPersistencePort,
            ProcessedEventPersistencePort processedEventPersistencePort,
            NotificationAuditPort notificationAuditPort,
            NotificationSearchCachePort notificationSearchCachePort,
            OutboxPersistencePort outboxPersistencePort,
            RecipientResolverPort recipientResolverPort,
            TemplateRendererPort templateRendererPort,
            NotificationProviderPort notificationProviderPort,
            DomainEventTopicPort domainEventTopicPort,
            ActorContextProviderPort actorContextProviderPort,
            ActorLegitimacyPort actorLegitimacyPort,
            ClockPort clockPort,
            NotificationResultMapper resultMapper) {
        this.requestPersistencePort = requestPersistencePort;
        this.attemptPersistencePort = attemptPersistencePort;
        this.templatePolicyPersistencePort = templatePolicyPersistencePort;
        this.providerCallbackPersistencePort = providerCallbackPersistencePort;
        this.readPersistencePort = readPersistencePort;
        this.processedEventPersistencePort = processedEventPersistencePort;
        this.notificationAuditPort = notificationAuditPort;
        this.notificationSearchCachePort = notificationSearchCachePort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.recipientResolverPort = recipientResolverPort;
        this.templateRendererPort = templateRendererPort;
        this.notificationProviderPort = notificationProviderPort;
        this.domainEventTopicPort = domainEventTopicPort;
        this.actorContextProviderPort = actorContextProviderPort;
        this.actorLegitimacyPort = actorLegitimacyPort;
        this.clockPort = clockPort;
        this.resultMapper = resultMapper;
    }

    @Override
    public Mono<NotificationResult> handle(EmitRelevantChangeNotificationCommand command) {
        Instant now = clockPort.now();
        String payloadHash = emissionIdempotencyHash(command);
        NotificationChannel channel = NotificationChannel.from(command.channel());
        NotificationKey notificationKey = NotificationKey.fromEventRecipientAndChannel(
                command.sourceEventId(),
                command.recipientRef(),
                channel);

        return requireActor(command.organizationId(), true)
                .then(checkIdempotency(command.organizationId(), "NOTIFICATION_EMITTED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> resolveOrCreateEmittedNotification(
                        command,
                        channel,
                        notificationKey,
                        now,
                        payloadHash,
                        idempotency));
    }

    private Mono<NotificationResult> resolveOrCreateEmittedNotification(
            EmitRelevantChangeNotificationCommand command,
            NotificationChannel channel,
            NotificationKey notificationKey,
            Instant now,
            String payloadHash,
            IdempotencyDecision idempotency) {
        if (idempotency.replayed()) {
            return findNotificationResult(command.organizationId(), idempotency.targetId())
                    .onErrorResume(NotificationResourceNotFoundException.class, error -> {
                        if (!isEventDrivenEmitIdempotency(command.idempotencyKey())) {
                            return Mono.error(error);
                        }
                        return locateOrCreateEmittedNotification(command, channel, notificationKey, now, payloadHash);
                    });
        }
        return locateOrCreateEmittedNotification(command, channel, notificationKey, now, payloadHash);
    }

    private Mono<NotificationResult> locateOrCreateEmittedNotification(
            EmitRelevantChangeNotificationCommand command,
            NotificationChannel channel,
            NotificationKey notificationKey,
            Instant now,
            String payloadHash) {
        return requestPersistencePort
                .findByKey(OrganizationId.of(command.organizationId()), notificationKey)
                .map(resultMapper::toNotificationResult)
                .switchIfEmpty(Mono.defer(() -> processedEventPersistencePort
                        .exists(command.sourceEventId(), PROCESSED_EVENT_CONSUMER)
                        .flatMap(processed -> {
                            if (processed) {
                                return requestPersistencePort
                                        .findByKey(OrganizationId.of(command.organizationId()), notificationKey)
                                        .switchIfEmpty(Mono.error(new ApplicationException(
                                                "evento_procesado_sin_notificacion",
                                                "Evento ya marcado como procesado y sin solicitud vinculada")))
                                        .map(resultMapper::toNotificationResult);
                            }
                            return createNotification(command, channel, notificationKey, now, payloadHash);
                        })));
    }

    private Mono<NotificationResult> createNotification(
            EmitRelevantChangeNotificationCommand command,
            NotificationChannel channel,
            NotificationKey notificationKey,
            Instant now,
            String payloadHash) {
        return templatePolicyPersistencePort
                .findActivePolicy(command.organizationId(), command.sourceEventType())
                .switchIfEmpty(Mono.error(new ApplicationException(
                        "policy_no_encontrada",
                        "No existe ChannelPolicy activa para sourceEventType=" + command.sourceEventType())))
                .flatMap(policy -> templatePolicyPersistencePort
                        .findActiveTemplate(command.organizationId(), command.sourceEventType(), channel.name())
                        .switchIfEmpty(Mono.error(new ApplicationException(
                                "template_no_encontrado",
                                "No existe NotificationTemplate activa para evento/canal solicitado")))
                        .flatMap(template -> templateRendererPort
                                .render(template.subjectTemplate(), template.bodyTemplate(), command.payloadJson())
                                .map(renderedPayload -> buildDispatch(command, policy, template, channel, notificationKey, renderedPayload, now))))
                .flatMap(dispatch -> requestPersistencePort
                        .create(dispatch.request())
                        .flatMap(created -> storeDomainEvents(dispatch.pullDomainEvents())
                                .then(afterMutation(
                                        command.organizationId(),
                                        "NOTIFICATION_EMITTED",
                                        "NotificationRequest",
                                        created.notificationId().value(),
                                        command.actorId(),
                                        command.idempotencyKey(),
                                        payloadHash,
                                        payloadJson("notificationId", created.notificationId().value()),
                                        new NotificationMutationEvent(
                                                "NotificationRequest",
                                                created.notificationId().value(),
                                                "NotificationEmitted",
                                                now)))
                                .then(processedEventPersistencePort.record(
                                        command.sourceEventId(),
                                        PROCESSED_EVENT_CONSUMER,
                                        now))
                                .thenReturn(created)))
                .map(resultMapper::toNotificationResult);
    }

    @Override
    public Mono<NotificationDetailResult> handle(DispatchNotificationCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.organizationId(), true)
                .then(checkIdempotency(command.organizationId(), "NOTIFICATION_DISPATCHED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findNotificationDetail(command.organizationId(), idempotency.targetId());
                    }
                    return dispatchInternal(
                            command.organizationId(),
                            command.actorId(),
                            command.notificationId(),
                            command.idempotencyKey(),
                            payloadHash,
                            "NOTIFICATION_DISPATCHED",
                            "NotificationDispatched",
                            now);
                });
    }

    @Override
    public Mono<NotificationDetailResult> handle(RetryNotificationCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.organizationId(), true)
                .then(checkIdempotency(command.organizationId(), "NOTIFICATION_RETRIED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findNotificationDetail(command.organizationId(), idempotency.targetId());
                    }
                    return dispatchInternal(
                            command.organizationId(),
                            command.actorId(),
                            command.notificationId(),
                            command.idempotencyKey(),
                            payloadHash,
                            "NOTIFICATION_RETRIED",
                            "NotificationRetried",
                            now);
                });
    }

    private Mono<NotificationDetailResult> dispatchInternal(
            String organizationId,
            String actorId,
            String notificationId,
            String idempotencyKey,
            String payloadHash,
            String actionType,
            String mutationEventType,
            Instant now) {
        return loadNotificationRequest(organizationId, notificationId)
                .flatMap(request -> templatePolicyPersistencePort
                        .findActivePolicy(organizationId, request.sourceEventType())
                        .switchIfEmpty(Mono.error(new ApplicationException(
                                "policy_no_encontrada",
                                "No existe ChannelPolicy activa para sourceEventType=" + request.sourceEventType())))
                        .flatMap(policy -> performDispatch(request, policy, now)
                                .flatMap(updatedRequest -> afterMutation(
                                                organizationId,
                                                actionType,
                                                "NotificationRequest",
                                                updatedRequest.notificationId().value(),
                                                actorId,
                                                idempotencyKey,
                                                payloadHash,
                                                payloadJson("notificationId", updatedRequest.notificationId().value()),
                                                new NotificationMutationEvent(
                                                        "NotificationRequest",
                                                        updatedRequest.notificationId().value(),
                                                        mutationEventType,
                                                        now))
                                        .then(findNotificationDetail(organizationId, updatedRequest.notificationId().value())))));
    }

    private Mono<NotificationRequest> performDispatch(NotificationRequest request, ChannelPolicy policy, Instant now) {
        return synchronizeRequestAttemptState(request, now)
                .flatMap(reconciledRequest -> {
                    NotificationDispatch dispatch = NotificationDispatch.rehydrate(reconciledRequest);
                    NotificationAttempt createdAttempt = dispatch.beginDispatchAttempt(policy, now);

                    return attemptPersistencePort
                            .create(createdAttempt, reconciledRequest.organizationId().value())
                            .flatMap(savedAttempt -> recipientResolverPort
                                    .resolve(
                                            reconciledRequest.organizationId().value(),
                                            reconciledRequest.recipientRef(),
                                            reconciledRequest.channel().name())
                                    .flatMap(recipient -> {
                                        if (!recipient.active()) {
                                            return failAttemptWithoutRecipient(
                                                    dispatch,
                                                    savedAttempt,
                                                    "recipient_inactive",
                                                    "recipientRef inactivo para canal solicitado",
                                                    now);
                                        }
                                        return sendAndPersistOutcome(dispatch, savedAttempt, recipient, policy, now);
                                    })
                                    .switchIfEmpty(Mono.defer(() -> failAttemptWithoutRecipient(
                                            dispatch,
                                            savedAttempt,
                                            "recipient_not_found",
                                            "No existe destinatario activo para recipientRef="
                                                    + reconciledRequest.recipientRef(),
                                            now))));
                });
    }

    private Mono<NotificationRequest> synchronizeRequestAttemptState(NotificationRequest request, Instant now) {
        return attemptPersistencePort
                .findByNotificationId(request.organizationId(), request.notificationId())
                .map(NotificationAttempt::attemptNumber)
                .reduce(0, Math::max)
                .flatMap(maxPersistedAttempt -> {
                    int originalAttemptCount = request.attemptCount();
                    NotificationRequestStatus originalStatus = request.status();
                    request.synchronizeAttemptCount(maxPersistedAttempt, now);
                    if (request.reachedMaxAttempts()
                            && request.status() != NotificationRequestStatus.DISCARDED
                            && request.status() != NotificationRequestStatus.SENT) {
                        request.discard(now);
                    }
                    boolean changed = originalAttemptCount != request.attemptCount()
                            || originalStatus != request.status();
                    if (!changed) {
                        return Mono.just(request);
                    }
                    return requestPersistencePort.update(request);
                })
                .defaultIfEmpty(request);
    }

    private Mono<NotificationRequest> failAttemptWithoutRecipient(
            NotificationDispatch dispatch,
            NotificationAttempt attempt,
            String errorCode,
            String errorMessage,
            Instant now) {
        dispatch.markAttemptFailed(
                attempt,
                errorCode,
                errorMessage,
                false,
                null,
                null,
                null,
                now);
        NotificationRequest updatedRequest = dispatch.request();
        return attemptPersistencePort
                .update(attempt, updatedRequest.organizationId().value())
                .then(requestPersistencePort.update(updatedRequest))
                .flatMap(persistedRequest -> storeDomainEvents(dispatch.pullDomainEvents()).thenReturn(persistedRequest));
    }

    private Mono<NotificationRequest> sendAndPersistOutcome(
            NotificationDispatch dispatch,
            NotificationAttempt attempt,
            RecipientResolution recipientResolution,
            ChannelPolicy policy,
            Instant now) {
        NotificationRequest request = dispatch.request();
        ProviderSendRequest sendRequest = new ProviderSendRequest(
                request.organizationId().value(),
                attempt.providerCode(),
                request.channel().name(),
                recipientResolution.destination(),
                request.payloadJson(),
                request.traceId(),
                request.correlationId());

        return notificationProviderPort
                .send(sendRequest)
                .flatMap(sendResult -> applySendResult(dispatch, attempt, sendResult, policy, now))
                .flatMap(tuple -> {
                    NotificationAttempt updatedAttempt = tuple.attempt;
                    NotificationRequest updatedRequest = tuple.request;
                    return attemptPersistencePort
                            .update(updatedAttempt, updatedRequest.organizationId().value())
                            .then(requestPersistencePort.update(updatedRequest))
                            .flatMap(persistedRequest -> storeDomainEvents(dispatch.pullDomainEvents()).thenReturn(persistedRequest));
                });
    }

    private Mono<AttemptAndRequest> applySendResult(
            NotificationDispatch dispatch,
            NotificationAttempt attempt,
            ProviderSendResult sendResult,
            ChannelPolicy policy,
            Instant now) {
        if (sendResult.success()) {
            dispatch.markAttemptSent(
                    attempt,
                    sendResult.providerRef(),
                    sendResult.latencyMs(),
                    sendResult.rawResponse(),
                    now);
            return Mono.just(new AttemptAndRequest(attempt, dispatch.request()));
        }

        boolean fallbackAvailable = policy.fallbackChannel() != null
                && policy.fallbackChannel() != dispatch.request().channel();
        boolean retryable = (sendResult.retryable() || fallbackAvailable) && !dispatch.request().reachedMaxAttempts();
        Instant nextRetryAt = retryable ? now.plusSeconds(policy.retryIntervalSeconds()) : null;
        dispatch.markAttemptFailed(
                attempt,
                sendResult.errorCode(),
                sendResult.errorMessage(),
                retryable,
                nextRetryAt,
                sendResult.latencyMs(),
                sendResult.rawResponse(),
                now);

        return Mono.just(new AttemptAndRequest(attempt, dispatch.request()));
    }

    @Override
    public Mono<NotificationResult> handle(DiscardNotificationCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.organizationId(), true)
                .then(checkIdempotency(command.organizationId(), "NOTIFICATION_DISCARDED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findNotificationResult(command.organizationId(), idempotency.targetId());
                    }
                    return loadNotificationRequest(command.organizationId(), command.notificationId())
                            .flatMap(request -> {
                                NotificationDispatch dispatch = NotificationDispatch.rehydrate(request);
                                dispatch.discard(now);
                                return requestPersistencePort.update(dispatch.request())
                                        .flatMap(updated -> afterMutation(
                                                        command.organizationId(),
                                                        "NOTIFICATION_DISCARDED",
                                                        "NotificationRequest",
                                                        updated.notificationId().value(),
                                                        command.actorId(),
                                                        command.idempotencyKey(),
                                                        payloadHash,
                                                        payloadJson("notificationId", updated.notificationId().value()),
                                                        new NotificationMutationEvent(
                                                                "NotificationRequest",
                                                                updated.notificationId().value(),
                                                                "NotificationDiscarded",
                                                                now))
                                                .thenReturn(updated));
                            })
                            .map(resultMapper::toNotificationResult);
                });
    }

    @Override
    public Mono<NotificationResult> handle(RecordNotificationDeliveryCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.organizationId(), true)
                .then(checkIdempotency(command.organizationId(), "NOTIFICATION_DELIVERY_RECORDED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findNotificationResult(command.organizationId(), idempotency.targetId());
                    }
                    return loadNotificationRequest(command.organizationId(), command.notificationId())
                            .flatMap(request -> {
                                NotificationDispatch dispatch = NotificationDispatch.rehydrate(request);
                                dispatch.reconcileDeliveryByCallback(command.providerCode(), command.providerRef(), now);
                                return requestPersistencePort.update(dispatch.request())
                                        .flatMap(updated -> storeDomainEvents(dispatch.pullDomainEvents())
                                                .then(afterMutation(
                                                        command.organizationId(),
                                                        "NOTIFICATION_DELIVERY_RECORDED",
                                                        "NotificationRequest",
                                                        updated.notificationId().value(),
                                                        command.actorId(),
                                                        command.idempotencyKey(),
                                                        payloadHash,
                                                        payloadJson("notificationId", updated.notificationId().value()),
                                                        new NotificationMutationEvent(
                                                                "NotificationRequest",
                                                                updated.notificationId().value(),
                                                                "NotificationDeliveryRecorded",
                                                                now)))
                                                .thenReturn(updated));
                            })
                            .map(resultMapper::toNotificationResult);
                });
    }

    @Override
    public Mono<NotificationDetailResult> handle(ProcessProviderCallbackCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.organizationId(), true)
                .then(checkIdempotency(command.organizationId(), "PROVIDER_CALLBACK_PROCESSED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findNotificationDetail(command.organizationId(), idempotency.targetId());
                    }
                    return providerCallbackPersistencePort
                            .findByProviderRefAndEvent(command.providerCode(), command.providerRef(), command.callbackEventId())
                            .flatMap(existing -> findNotificationDetail(command.organizationId(), existing.notificationId()))
                            .switchIfEmpty(Mono.defer(() -> loadNotificationRequest(command.organizationId(), command.notificationId())
                                    .flatMap(request -> {
                                        ProviderCallbackStatus callbackStatus = ProviderCallbackStatus.valueOf(command.callbackStatus().trim().toUpperCase());
                                        ProviderCallback callback = new ProviderCallback(
                                                UUID.randomUUID().toString(),
                                                command.organizationId(),
                                                request.notificationId().value(),
                                                command.providerCode(),
                                                command.providerRef(),
                                                command.callbackEventId(),
                                                callbackStatus,
                                                command.payload(),
                                                now,
                                                now);
                                        return providerCallbackPersistencePort
                                                .create(callback)
                                                .flatMap(saved -> {
                                                    Mono<NotificationRequest> requestUpdate;
                                                    if (callbackStatus == ProviderCallbackStatus.VALIDATED) {
                                                        NotificationDispatch dispatch = NotificationDispatch.rehydrate(request);
                                                        dispatch.reconcileDeliveryByCallback(saved.providerCode(), saved.providerRef(), now);
                                                        requestUpdate = requestPersistencePort
                                                                .update(dispatch.request())
                                                                .flatMap(updated -> storeDomainEvents(dispatch.pullDomainEvents())
                                                                        .thenReturn(updated));
                                                    } else {
                                                        requestUpdate = Mono.just(request);
                                                    }
                                                    return requestUpdate
                                                            .flatMap(updated -> afterMutation(
                                                                            command.organizationId(),
                                                                            "PROVIDER_CALLBACK_PROCESSED",
                                                                            "NotificationRequest",
                                                                            updated.notificationId().value(),
                                                                            command.actorId(),
                                                                            command.idempotencyKey(),
                                                                            payloadHash,
                                                                            payloadJson("notificationId", updated.notificationId().value()),
                                                                            new NotificationMutationEvent(
                                                                                    "NotificationRequest",
                                                                                    updated.notificationId().value(),
                                                                                    "ProviderCallbackProcessed",
                                                                                    now))
                                                                    .then(findNotificationDetail(command.organizationId(), updated.notificationId().value())));
                                                });
                                    })));
                });
    }

    @Override
    public Mono<NotificationDetailResult> handle(ReprocessNotificationDlqCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.organizationId(), true)
                .then(checkIdempotency(command.organizationId(), "NOTIFICATION_DLQ_REPROCESSED", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findNotificationDetail(command.organizationId(), idempotency.targetId());
                    }
                    return processedEventPersistencePort
                            .exists(command.dlqEventId(), command.consumerName())
                            .flatMap(alreadyProcessed -> {
                                if (alreadyProcessed) {
                                    return findNotificationDetail(command.organizationId(), command.notificationId());
                                }
                                return dispatchInternal(
                                                command.organizationId(),
                                                command.actorId(),
                                                command.notificationId(),
                                                command.idempotencyKey(),
                                                payloadHash,
                                                "NOTIFICATION_DLQ_REPROCESSED",
                                                "NotificationDlqReprocessed",
                                                now)
                                        .flatMap(result -> processedEventPersistencePort
                                                .record(command.dlqEventId(), command.consumerName(), now)
                                                .thenReturn(result));
                            });
                });
    }

    @Override
    public Mono<NotificationResult> handle(GetNotificationByIdQuery query) {
        return requireActor(query.organizationId(), false).then(findNotificationResult(query.organizationId(), query.notificationId()));
    }

    @Override
    public Mono<NotificationSearchResult> handle(SearchNotificationsQuery query) {
        return requireActor(query.organizationId(), false)
                .then(Mono.defer(() -> {
                    String cacheKey = cacheKeyFor(query);
                    return notificationSearchCachePort
                            .get(cacheKey)
                            .switchIfEmpty(readPersistencePort
                                    .search(toFilter(query))
                                    .map(resultMapper::toSearchItemResult)
                                    .collectList()
                                    .zipWith(readPersistencePort.count(toFilter(query)))
                                    .map(tuple -> new NotificationSearchResult(
                                            tuple.getT1(),
                                            Math.max(query.page(), 0),
                                            query.size(),
                                            tuple.getT2()))
                                    .flatMap(result -> notificationSearchCachePort.put(cacheKey, result).thenReturn(result)));
                }));
    }

    @Override
    public Mono<NotificationDetailResult> handle(GetNotificationDetailQuery query) {
        return requireActor(query.organizationId(), false).then(findNotificationDetail(query.organizationId(), query.notificationId()));
    }

    @Override
    public Flux<NotificationAttemptResult> handle(ListNotificationAttemptsQuery query) {
        return requireActor(query.organizationId(), false)
                .thenMany(attemptPersistencePort
                        .findByNotificationId(OrganizationId.of(query.organizationId()), NotificationId.of(query.notificationId()))
                        .map(resultMapper::toAttemptResult));
    }

    @Override
    public Mono<NotificationTimelineResult> handle(GetNotificationTimelineQuery query) {
        return requireActor(query.organizationId(), false)
                .then(findNotificationDetail(query.organizationId(), query.notificationId()))
                .map(detail -> {
                    List<NotificationTimelineItemResult> items = new ArrayList<>();
                    for (NotificationAttemptResult attempt : detail.attempts()) {
                        items.add(new NotificationTimelineItemResult(
                                "ATTEMPT",
                                attempt.attemptId(),
                                attempt.resultStatus(),
                                attempt.providerRef(),
                                attempt.createdAt(),
                                attempt.errorMessage()));
                    }
                    for (ProviderCallbackResult callback : detail.callbacks()) {
                        items.add(new NotificationTimelineItemResult(
                                "CALLBACK",
                                callback.callbackId(),
                                callback.callbackStatus(),
                                callback.providerRef(),
                                callback.receivedAt(),
                                ""));
                    }
                    items.sort(Comparator.comparing(NotificationTimelineItemResult::occurredAt));
                    return new NotificationTimelineResult(detail.notification().notificationId(), items);
                });
    }

    @Override
    public Mono<NotificationMetricsResult> handle(GetNotificationMetricsQuery query) {
        return requireActor(query.organizationId(), true)
                .then(readPersistencePort.metrics(query.organizationId()).defaultIfEmpty(new NotificationMetricsProjection(
                        0L,
                        null,
                        null,
                        null,
                        null)))
                .map(resultMapper::toMetricsResult);
    }

    @Override
    public Mono<NotificationAuditResult> handle(GetNotificationAuditQuery query) {
        return requireActor(query.organizationId(), true)
                .then(notificationAuditPort
                        .findByTarget(query.organizationId(), query.targetType(), query.targetId(), query.page() * query.size(), query.size())
                        .map(resultMapper::toAuditEntryResult)
                        .collectList()
                        .zipWith(notificationAuditPort.countByTarget(query.organizationId(), query.targetType(), query.targetId()))
                        .map(tuple -> new NotificationAuditResult(tuple.getT1(), query.page(), query.size(), tuple.getT2())));
    }

    private NotificationDispatch buildDispatch(
            EmitRelevantChangeNotificationCommand command,
            ChannelPolicy policy,
            NotificationTemplate template,
            NotificationChannel channel,
            NotificationKey notificationKey,
            String renderedPayload,
            Instant now) {
        RelevantChangeNotification relevantChangeNotification = new RelevantChangeNotification(
                OrganizationId.of(command.organizationId()),
                command.sourceEventId(),
                command.sourceEventType(),
                command.recipientRef(),
                channel,
                renderedPayload,
                command.traceId(),
                command.correlationId());
        return NotificationDispatch.emit(
                relevantChangeNotification,
                template.templateId(),
                policy.policyId(),
                renderedPayload,
                policy.maxAttempts(),
                now,
                now);
    }

    private Mono<NotificationRequest> loadNotificationRequest(String organizationId, String notificationId) {
        return requestPersistencePort
                .findById(OrganizationId.of(organizationId), NotificationId.of(notificationId))
                .switchIfEmpty(Mono.error(new NotificationResourceNotFoundException("NotificationRequest", notificationId)));
    }

    private Mono<NotificationResult> findNotificationResult(String organizationId, String notificationId) {
        return loadNotificationRequest(organizationId, notificationId).map(resultMapper::toNotificationResult);
    }

    private Mono<NotificationDetailResult> findNotificationDetail(String organizationId, String notificationId) {
        return loadNotificationRequest(organizationId, notificationId)
                .flatMap(request -> attemptPersistencePort
                        .findByNotificationId(request.organizationId(), request.notificationId())
                        .map(resultMapper::toAttemptResult)
                        .collectList()
                        .zipWith(providerCallbackPersistencePort
                                .findByNotificationId(organizationId, notificationId)
                                .map(resultMapper::toCallbackResult)
                                .collectList())
                        .map(tuple -> new NotificationDetailResult(
                                resultMapper.toNotificationResult(request),
                                tuple.getT1(),
                                tuple.getT2())));
    }

    private Mono<Void> storeDomainEvents(List<DomainEvent> events) {
        return Flux.fromIterable(events)
                .concatMap(event -> outboxPersistencePort.store(event, domainEventPayload(event)))
                .then();
    }

    private Mono<Void> requireActor(String organizationId, boolean adminRequired) {
        if (organizationId == null || organizationId.isBlank()) {
            return Mono.error(new ApplicationException("organization_requerida", "organizationId es obligatorio"));
        }
        return actorContextProviderPort.currentActor()
                .switchIfEmpty(Mono.error(new OperationNotPermittedException(
                        "actor_no_autenticado",
                        "No hay actor autenticado disponible para ejecutar la operacion")))
                .flatMap(actor -> {
                    if (adminRequired && !actor.admin() && !actor.internalActor()) {
                        return Mono.error(new OperationNotPermittedException(
                                "operacion_no_permitida",
                                "La operacion requiere rol administrativo o servicio tecnico"));
                    }
                    if (!actor.admin() && !actor.internalActor() && !organizationId.equals(actor.organizationId())) {
                        return Mono.error(new ApplicationException(
                                "acceso_cross_organization", "Actor no autorizado para el organization solicitado"));
                    }
                    if (actor.internalActor()) {
                        return Mono.empty();
                    }
                    return actorLegitimacyPort
                            .isLegitimate(actor.actorId(), organizationId)
                            .flatMap(valid -> valid ? Mono.empty() : Mono.error(new ActorNotLegitimateException()));
                });
    }

    private Mono<IdempotencyDecision> checkIdempotency(
            String organizationId,
            String actionType,
            String idempotencyKey,
            String payloadHash) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.just(IdempotencyDecision.none());
        }
        return notificationAuditPort
                .findByIdempotency(organizationId, actionType, idempotencyKey)
                .flatMap(existing -> {
                    if (!payloadHash.equals(existing.payloadHash())) {
                        if (isEventDrivenEmitIdempotency(actionType, idempotencyKey)) {
                            return Mono.just(new IdempotencyDecision(true, existing.targetId()));
                        }
                        return Mono.error(new IdempotencyConflictException());
                    }
                    return Mono.just(new IdempotencyDecision(true, existing.targetId()));
                })
                .switchIfEmpty(Mono.just(IdempotencyDecision.none()));
    }

    private Mono<Void> afterMutation(
            String organizationId,
            String actionType,
            String targetType,
            String targetId,
            String actorId,
            String idempotencyKey,
            String payloadHash,
            String payload,
            DomainEvent optionalEvent) {
        Instant now = clockPort.now();
        NotificationAuditEntry audit = new NotificationAuditEntry(
                UUID.randomUUID().toString(),
                organizationId,
                actorId,
                actionType,
                targetType,
                targetId,
                "SUCCESS",
                payload,
                idempotencyKey,
                payloadHash,
                now);

        Mono<Void> storeAudit = notificationAuditPort
                .record(audit)
                .onErrorResume(DuplicateKeyException.class, error -> {
                    if (isEventDrivenEmitIdempotency(actionType, idempotencyKey)) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                });
        Mono<Void> storeEvent = optionalEvent == null
                ? Mono.empty()
                : outboxPersistencePort.store(optionalEvent, payloadWithEventType(payload, optionalEvent.eventType()));

        return storeAudit.then(storeEvent).then(notificationSearchCachePort.evictOrganization(organizationId));
    }

    private String payloadWithEventType(String payload, String eventType) {
        return "{\"eventType\":\"" + eventType + "\",\"payload\":" + payload + "}";
    }

    private String domainEventPayload(DomainEvent event) {
        String topic = domainEventTopicPort.topicFor(event.eventType());
        return "{"
                + "\"eventId\":\"" + event.eventId() + "\"," 
                + "\"eventType\":\"" + event.eventType() + "\"," 
                + "\"topic\":\"" + topic + "\"," 
                + "\"aggregateType\":\"" + event.aggregateType() + "\"," 
                + "\"aggregateId\":\"" + event.aggregateId() + "\"," 
                + "\"occurredAt\":\"" + event.occurredAt() + "\""
                + "}";
    }

    private String payloadJson(String key, String value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    private NotificationSearchFilter toFilter(SearchNotificationsQuery query) {
        int safeSize = query.size() <= 0 ? 20 : Math.min(query.size(), 100);
        int safePage = Math.max(query.page(), 0);
        return new NotificationSearchFilter(
                query.organizationId(),
                query.status(),
                query.sourceEventType(),
                query.channel(),
                query.recipientRef(),
                safePage * safeSize,
                safeSize);
    }

    private String cacheKeyFor(SearchNotificationsQuery query) {
        return String.join(
                "::",
                query.organizationId(),
                String.valueOf(query.page()),
                String.valueOf(query.size()),
                nullSafe(query.status()),
                nullSafe(query.sourceEventType()),
                nullSafe(query.channel()),
                nullSafe(query.recipientRef()));
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String emissionIdempotencyHash(EmitRelevantChangeNotificationCommand command) {
        if (!isEventDrivenEmitIdempotency(command.idempotencyKey())) {
            return IdempotencySupport.payloadHash(command.toString());
        }
        String fingerprint = String.join(
                "::",
                nullSafe(command.organizationId()),
                nullSafe(command.sourceEventId()),
                nullSafe(command.sourceEventType()),
                nullSafe(command.channel()));
        return IdempotencySupport.payloadHash(fingerprint);
    }

    private boolean isEventDrivenEmitIdempotency(String idempotencyKey) {
        return isEventDrivenEmitIdempotency("NOTIFICATION_EMITTED", idempotencyKey);
    }

    private boolean isEventDrivenEmitIdempotency(String actionType, String idempotencyKey) {
        return "NOTIFICATION_EMITTED".equals(actionType)
                && idempotencyKey != null
                && idempotencyKey.startsWith("kafka-emit-");
    }

    private record IdempotencyDecision(boolean replayed, String targetId) {

        private static IdempotencyDecision none() {
            return new IdempotencyDecision(false, null);
        }
    }

    private record AttemptAndRequest(NotificationAttempt attempt, NotificationRequest request) {
    }
}
