package com.arka.notification.infrastructure.adapter.out.persistence;

import com.arka.notification.application.exception.OptimisticLockingFailureException;
import com.arka.notification.application.port.out.audit.NotificationAuditEntry;
import com.arka.notification.application.port.out.audit.NotificationAuditPort;
import com.arka.notification.application.port.out.persistence.NotificationAttemptPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationMetricsProjection;
import com.arka.notification.application.port.out.persistence.NotificationReadPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationSearchFilter;
import com.arka.notification.application.port.out.persistence.NotificationSearchProjection;
import com.arka.notification.application.port.out.persistence.NotificationTemplatePolicyPersistencePort;
import com.arka.notification.application.port.out.persistence.OutboxPersistencePort;
import com.arka.notification.application.port.out.persistence.OutboxRelayPort;
import com.arka.notification.application.port.out.persistence.PendingOutboxEvent;
import com.arka.notification.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.notification.application.port.out.persistence.ProviderCallbackPersistencePort;
import com.arka.notification.application.port.out.persistence.ProviderCallbackProjection;
import com.arka.notification.domain.notificationdispatch.entity.ChannelPolicy;
import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.entity.NotificationTemplate;
import com.arka.notification.domain.notificationdispatch.entity.ProviderCallback;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.TenantId;
import com.arka.notification.domain.shared.event.DomainEvent;
import com.arka.notification.infrastructure.adapter.out.persistence.entity.ProcessedEventRow;
import com.arka.notification.infrastructure.adapter.out.persistence.mapper.NotificationRowMapper;
import com.arka.notification.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveChannelPolicyRepository;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveNotificationAttemptRepository;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveNotificationAuditRepository;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveNotificationRequestRepository;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveNotificationTemplateRepository;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveProcessedEventRepository;
import com.arka.notification.infrastructure.adapter.out.persistence.repository.ReactiveProviderCallbackRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class NotificationR2dbcPersistenceAdapter
        implements NotificationRequestPersistencePort,
                NotificationAttemptPersistencePort,
                NotificationTemplatePolicyPersistencePort,
                ProviderCallbackPersistencePort,
                NotificationReadPersistencePort,
                ProcessedEventPersistencePort,
                OutboxPersistencePort,
                OutboxRelayPort,
                NotificationAuditPort {

    private final ReactiveNotificationRequestRepository requestRepository;
    private final ReactiveNotificationAttemptRepository attemptRepository;
    private final ReactiveNotificationTemplateRepository templateRepository;
    private final ReactiveChannelPolicyRepository channelPolicyRepository;
    private final ReactiveProviderCallbackRepository providerCallbackRepository;
    private final ReactiveNotificationAuditRepository auditRepository;
    private final ReactiveOutboxEventRepository outboxEventRepository;
    private final ReactiveProcessedEventRepository processedEventRepository;
    private final NotificationRowMapper rowMapper;
    private final OutboxRowMapper outboxRowMapper;
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate entityTemplate;

    public NotificationR2dbcPersistenceAdapter(
            ReactiveNotificationRequestRepository requestRepository,
            ReactiveNotificationAttemptRepository attemptRepository,
            ReactiveNotificationTemplateRepository templateRepository,
            ReactiveChannelPolicyRepository channelPolicyRepository,
            ReactiveProviderCallbackRepository providerCallbackRepository,
            ReactiveNotificationAuditRepository auditRepository,
            ReactiveOutboxEventRepository outboxEventRepository,
            ReactiveProcessedEventRepository processedEventRepository,
            NotificationRowMapper rowMapper,
            OutboxRowMapper outboxRowMapper,
            DatabaseClient databaseClient,
            R2dbcEntityTemplate entityTemplate) {
        this.requestRepository = requestRepository;
        this.attemptRepository = attemptRepository;
        this.templateRepository = templateRepository;
        this.channelPolicyRepository = channelPolicyRepository;
        this.providerCallbackRepository = providerCallbackRepository;
        this.auditRepository = auditRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
        this.rowMapper = rowMapper;
        this.outboxRowMapper = outboxRowMapper;
        this.databaseClient = databaseClient;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<NotificationRequest> create(NotificationRequest request) {
        return entityTemplate.insert(rowMapper.toRow(request)).map(rowMapper::toDomain);
    }

    @Override
    public Mono<NotificationRequest> update(NotificationRequest request) {
        long expectedVersion = request.version();
        long nextVersion = expectedVersion + 1;
        return requestRepository
                .updateOptimistic(
                        request.tenantId().value(),
                        request.notificationId().value(),
                        request.templateId(),
                        request.channelPolicyId(),
                        request.payloadJson(),
                        request.status().name(),
                        request.retryable(),
                        request.nextRetryAt(),
                        request.maxAttempts(),
                        request.attemptCount(),
                        request.traceId(),
                        request.correlationId(),
                        expectedVersion,
                        nextVersion,
                        request.updatedAt())
                .flatMap(updatedRows -> {
                    if (updatedRows == null || updatedRows == 0) {
                        return Mono.error(new OptimisticLockingFailureException());
                    }
                    return findById(request.tenantId(), request.notificationId());
                });
    }

    @Override
    public Mono<NotificationRequest> findById(TenantId tenantId, NotificationId notificationId) {
        return requestRepository.findByTenantAndId(tenantId.value(), notificationId.value()).map(rowMapper::toDomain);
    }

    @Override
    public Mono<NotificationRequest> findByKey(TenantId tenantId, NotificationKey notificationKey) {
        return requestRepository.findByTenantAndKey(tenantId.value(), notificationKey.value()).map(rowMapper::toDomain);
    }

    @Override
    public Flux<NotificationRequest> findDispatchable(Instant at, int limit) {
        int safeLimit = Math.max(1, limit);
        return requestRepository.findDispatchable(at, safeLimit).map(rowMapper::toDomain);
    }

    @Override
    public Mono<NotificationAttempt> create(NotificationAttempt attempt, String tenantId) {
        return entityTemplate.insert(rowMapper.toRow(attempt, tenantId)).map(rowMapper::toDomain);
    }

    @Override
    public Mono<NotificationAttempt> update(NotificationAttempt attempt, String tenantId) {
        var row = rowMapper.toRow(attempt, tenantId);
        return attemptRepository.existsById(row.attemptId())
                .flatMap(exists -> exists ? attemptRepository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Flux<NotificationAttempt> findByNotificationId(TenantId tenantId, NotificationId notificationId) {
        return attemptRepository.findByNotificationId(tenantId.value(), notificationId.value()).map(rowMapper::toDomain);
    }

    @Override
    public Mono<NotificationTemplate> findActiveTemplate(String tenantId, String sourceEventType, String channel) {
        return templateRepository.findActive(tenantId, sourceEventType, channel).map(rowMapper::toDomain);
    }

    @Override
    public Mono<ChannelPolicy> findActivePolicy(String tenantId, String sourceEventType) {
        return channelPolicyRepository.findActive(tenantId, sourceEventType).map(rowMapper::toDomain);
    }

    @Override
    public Mono<ProviderCallback> create(ProviderCallback callback) {
        return providerCallbackRepository
                .existsById(callback.callbackId())
                .flatMap(exists -> exists
                        ? providerCallbackRepository.findById(callback.callbackId())
                        : entityTemplate.insert(rowMapper.toRow(callback)))
                .map(rowMapper::toDomain)
                .onErrorResume(
                        throwable -> isDuplicate(throwable),
                        throwable -> findByProviderRefAndEvent(
                                callback.providerCode(),
                                callback.providerRef(),
                                callback.callbackEventId()));
    }

    @Override
    public Mono<ProviderCallback> findByProviderRefAndEvent(String providerCode, String providerRef, String callbackEventId) {
        return providerCallbackRepository
                .findByProviderRefAndEvent(providerCode, providerRef, callbackEventId)
                .map(rowMapper::toDomain);
    }

    @Override
    public Flux<ProviderCallbackProjection> findByNotificationId(String tenantId, String notificationId) {
        return providerCallbackRepository
                .findByNotificationId(tenantId, notificationId)
                .map(rowMapper::toProjection);
    }

    @Override
    public Flux<NotificationSearchProjection> search(NotificationSearchFilter filter) {
        String sql = """
                SELECT notification_id,
                       source_event_type,
                       recipient_ref,
                       channel,
                       status,
                       attempt_count,
                       updated_at
                FROM notification_requests
                WHERE tenant_id = :tenantId
                  AND (:status IS NULL OR status = :status)
                  AND (:sourceEventType IS NULL OR source_event_type = :sourceEventType)
                  AND (:channel IS NULL OR channel = :channel)
                  AND (:recipientRef IS NULL OR recipient_ref = :recipientRef)
                ORDER BY updated_at DESC
                OFFSET :offset
                LIMIT :limit
                """;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tenantId", filter.tenantId())
                .bind("offset", filter.offset())
                .bind("limit", filter.limit());

        spec = bindNullable(spec, "status", filter.status());
        spec = bindNullable(spec, "sourceEventType", filter.sourceEventType());
        spec = bindNullable(spec, "channel", filter.channel());
        spec = bindNullable(spec, "recipientRef", filter.recipientRef());

        return spec
                .map((row, metadata) -> new NotificationSearchProjection(
                        row.get("notification_id", String.class),
                        row.get("source_event_type", String.class),
                        row.get("recipient_ref", String.class),
                        row.get("channel", String.class),
                        row.get("status", String.class),
                        row.get("attempt_count", Integer.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<Long> count(NotificationSearchFilter filter) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM notification_requests
                WHERE tenant_id = :tenantId
                  AND (:status IS NULL OR status = :status)
                  AND (:sourceEventType IS NULL OR source_event_type = :sourceEventType)
                  AND (:channel IS NULL OR channel = :channel)
                  AND (:recipientRef IS NULL OR recipient_ref = :recipientRef)
                """;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql).bind("tenantId", filter.tenantId());
        spec = bindNullable(spec, "status", filter.status());
        spec = bindNullable(spec, "sourceEventType", filter.sourceEventType());
        spec = bindNullable(spec, "channel", filter.channel());
        spec = bindNullable(spec, "recipientRef", filter.recipientRef());

        return spec
                .map((row, metadata) -> row.get("total", Long.class))
                .one()
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<NotificationMetricsProjection> metrics(String tenantId) {
        String sql = """
                WITH request_stats AS (
                    SELECT
                        COUNT(*)::bigint AS total_requests,
                        COUNT(*) FILTER (WHERE status = 'SENT')::bigint AS sent_requests,
                        COUNT(*) FILTER (WHERE status = 'DISCARDED')::bigint AS discarded_requests,
                        COUNT(*) FILTER (
                            WHERE status IN ('PENDING', 'FAILED')
                              AND retryable = TRUE
                              AND (next_retry_at IS NULL OR next_retry_at <= NOW())
                        )::bigint AS pending_dispatch_count,
                        AVG(CASE WHEN status = 'SENT' THEN attempt_count::numeric END) AS mean_attempts_to_success
                    FROM notification_requests
                    WHERE tenant_id = :tenantId
                ),
                attempt_stats AS (
                    SELECT
                        COUNT(*) FILTER (WHERE result_status = 'FAILED')::bigint AS failed_attempts,
                        COUNT(*) FILTER (
                            WHERE result_status = 'FAILED'
                              AND error_code IN ('PROVIDER_TIMEOUT', 'TIMEOUT')
                        )::bigint AS timeout_failed_attempts
                    FROM notification_attempts
                    WHERE tenant_id = :tenantId
                )
                SELECT
                    rs.pending_dispatch_count,
                    COALESCE(ROUND((rs.sent_requests::numeric / NULLIF(rs.total_requests, 0)::numeric) * 100, 2), 0) AS delivery_success_rate,
                    COALESCE(ROUND((rs.discarded_requests::numeric / NULLIF(rs.total_requests, 0)::numeric) * 100, 2), 0) AS discard_rate,
                    COALESCE(ROUND(rs.mean_attempts_to_success, 2), 0) AS mean_attempts_to_success,
                    COALESCE(ROUND((ast.timeout_failed_attempts::numeric / NULLIF(ast.failed_attempts, 0)::numeric) * 100, 2), 0) AS provider_timeout_rate
                FROM request_stats rs
                CROSS JOIN attempt_stats ast
                """;

        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new NotificationMetricsProjection(
                        row.get("pending_dispatch_count", Long.class) == null ? 0L : row.get("pending_dispatch_count", Long.class),
                        defaultDecimal(row.get("delivery_success_rate", BigDecimal.class)),
                        defaultDecimal(row.get("discard_rate", BigDecimal.class)),
                        defaultDecimal(row.get("mean_attempts_to_success", BigDecimal.class)),
                        defaultDecimal(row.get("provider_timeout_rate", BigDecimal.class))))
                .one();
    }

    @Override
    public Mono<Boolean> exists(String eventId, String consumerName) {
        return processedEventRepository.existsByEventAndConsumer(eventId, consumerName).defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> record(String eventId, String consumerName, Instant processedAt) {
        ProcessedEventRow row = new ProcessedEventRow(
                UUID.randomUUID().toString(),
                eventId,
                consumerName,
                processedAt);
        return entityTemplate
                .insert(row)
                .then()
                .onErrorResume(throwable -> isDuplicate(throwable), throwable -> Mono.empty());
    }

    @Override
    public Mono<Void> store(DomainEvent event, String payload) {
        return entityTemplate.insert(outboxRowMapper.toRow(event, payload)).then();
    }

    @Override
    public Flux<PendingOutboxEvent> findPending(int limit) {
        return outboxEventRepository.findPending(Math.max(1, limit)).map(row -> new PendingOutboxEvent(
                row.eventId(),
                row.aggregateType(),
                row.aggregateId(),
                row.eventType(),
                row.payload(),
                row.retryCount() == null ? 0 : row.retryCount()));
    }

    @Override
    public Mono<Void> markPublished(String eventId, Instant publishedAt) {
        return outboxEventRepository.markPublished(eventId, publishedAt).then();
    }

    @Override
    public Mono<Void> markFailed(String eventId, String errorMessage, Instant updatedAt, int maxRetries) {
        return outboxEventRepository.markFailed(eventId, errorMessage, updatedAt, maxRetries).then();
    }

    @Override
    public Mono<Void> record(NotificationAuditEntry entry) {
        return entityTemplate.insert(rowMapper.toRow(entry)).then();
    }

    @Override
    public Mono<NotificationAuditEntry> findByIdempotency(String tenantId, String actionType, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.empty();
        }
        return auditRepository.findByIdempotency(tenantId, actionType, idempotencyKey).map(rowMapper::toDomain);
    }

    @Override
    public Flux<NotificationAuditEntry> findByTarget(String tenantId, String targetType, String targetId, int offset, int limit) {
        return auditRepository
                .findByTarget(tenantId, emptyAsNull(targetType), emptyAsNull(targetId), offset, limit)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Long> countByTarget(String tenantId, String targetType, String targetId) {
        return auditRepository.countByTarget(tenantId, emptyAsNull(targetType), emptyAsNull(targetId));
    }

    private DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec,
            String name,
            String value) {
        if (value == null || value.isBlank()) {
            return spec.bindNull(name, String.class);
        }
        return spec.bind(name, value.trim());
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String emptyAsNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private boolean isDuplicate(Throwable throwable) {
        return throwable instanceof DuplicateKeyException || throwable instanceof DataIntegrityViolationException;
    }
}
