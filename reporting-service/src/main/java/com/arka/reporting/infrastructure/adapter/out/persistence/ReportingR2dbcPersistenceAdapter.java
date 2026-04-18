package com.arka.reporting.infrastructure.adapter.out.persistence;

import com.arka.reporting.application.exception.OptimisticLockingFailureException;
import com.arka.reporting.application.port.out.audit.ReportingAuditEntry;
import com.arka.reporting.application.port.out.audit.ReportingAuditPort;
import com.arka.reporting.application.port.out.persistence.AnalyticFactPersistencePort;
import com.arka.reporting.application.port.out.persistence.ConsumerCheckpointPersistencePort;
import com.arka.reporting.application.port.out.persistence.FactSearchFilter;
import com.arka.reporting.application.port.out.persistence.FactSearchProjection;
import com.arka.reporting.application.port.out.persistence.OutboxPersistencePort;
import com.arka.reporting.application.port.out.persistence.OutboxRelayPort;
import com.arka.reporting.application.port.out.persistence.PendingOutboxEvent;
import com.arka.reporting.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.reporting.application.port.out.persistence.ProjectionPersistencePort;
import com.arka.reporting.application.port.out.persistence.ReportArtifactPersistencePort;
import com.arka.reporting.application.port.out.persistence.ReportingMetricsProjection;
import com.arka.reporting.application.port.out.persistence.ReportingReadPersistencePort;
import com.arka.reporting.application.port.out.persistence.WeeklyReportExecutionPersistencePort;
import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.valueobject.FactId;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.shared.event.DomainEvent;
import com.arka.reporting.domain.weeklyreportexecution.entity.ConsumerCheckpoint;
import com.arka.reporting.domain.weeklyreportexecution.entity.OperationsKpiProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReportArtifact;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReplenishmentProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.SalesProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.ExecutionId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.AnalyticFactRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.OperationsKpiProjectionRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ProcessedEventRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ReplenishmentProjectionRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.SalesProjectionRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import com.arka.reporting.infrastructure.adapter.out.persistence.mapper.ReportingRowMapper;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveAnalyticFactRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveConsumerCheckpointRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveOperationsKpiProjectionRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveProcessedEventRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveReportArtifactRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveReplenishmentProjectionRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveReportingAuditRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveSalesProjectionRepository;
import com.arka.reporting.infrastructure.adapter.out.persistence.repository.ReactiveWeeklyReportExecutionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReportingR2dbcPersistenceAdapter
        implements AnalyticFactPersistencePort,
                ProjectionPersistencePort,
                WeeklyReportExecutionPersistencePort,
                ReportArtifactPersistencePort,
                ConsumerCheckpointPersistencePort,
                ReportingReadPersistencePort,
                ProcessedEventPersistencePort,
                OutboxPersistencePort,
                OutboxRelayPort,
                ReportingAuditPort {

    private final ReactiveAnalyticFactRepository analyticFactRepository;
    private final ReactiveSalesProjectionRepository salesProjectionRepository;
    private final ReactiveReplenishmentProjectionRepository replenishmentProjectionRepository;
    private final ReactiveOperationsKpiProjectionRepository operationsKpiProjectionRepository;
    private final ReactiveWeeklyReportExecutionRepository weeklyReportExecutionRepository;
    private final ReactiveReportArtifactRepository reportArtifactRepository;
    private final ReactiveConsumerCheckpointRepository consumerCheckpointRepository;
    private final ReactiveReportingAuditRepository reportingAuditRepository;
    private final ReactiveOutboxEventRepository outboxEventRepository;
    private final ReactiveProcessedEventRepository processedEventRepository;
    private final ReportingRowMapper rowMapper;
    private final OutboxRowMapper outboxRowMapper;
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate entityTemplate;

    public ReportingR2dbcPersistenceAdapter(
            ReactiveAnalyticFactRepository analyticFactRepository,
            ReactiveSalesProjectionRepository salesProjectionRepository,
            ReactiveReplenishmentProjectionRepository replenishmentProjectionRepository,
            ReactiveOperationsKpiProjectionRepository operationsKpiProjectionRepository,
            ReactiveWeeklyReportExecutionRepository weeklyReportExecutionRepository,
            ReactiveReportArtifactRepository reportArtifactRepository,
            ReactiveConsumerCheckpointRepository consumerCheckpointRepository,
            ReactiveReportingAuditRepository reportingAuditRepository,
            ReactiveOutboxEventRepository outboxEventRepository,
            ReactiveProcessedEventRepository processedEventRepository,
            ReportingRowMapper rowMapper,
            OutboxRowMapper outboxRowMapper,
            DatabaseClient databaseClient,
            R2dbcEntityTemplate entityTemplate) {
        this.analyticFactRepository = analyticFactRepository;
        this.salesProjectionRepository = salesProjectionRepository;
        this.replenishmentProjectionRepository = replenishmentProjectionRepository;
        this.operationsKpiProjectionRepository = operationsKpiProjectionRepository;
        this.weeklyReportExecutionRepository = weeklyReportExecutionRepository;
        this.reportArtifactRepository = reportArtifactRepository;
        this.consumerCheckpointRepository = consumerCheckpointRepository;
        this.reportingAuditRepository = reportingAuditRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
        this.rowMapper = rowMapper;
        this.outboxRowMapper = outboxRowMapper;
        this.databaseClient = databaseClient;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<AnalyticFact> create(AnalyticFact fact) {
        AnalyticFactRow row = rowMapper.toRow(fact, derivePeriod(fact.occurredAt()));
        return entityTemplate
                .insert(row)
                .map(rowMapper::toDomain)
                .onErrorResume(this::isDuplicate, throwable -> findBySourceEventId(
                        com.arka.reporting.domain.analyticfact.valueobject.OrganizationId.of(fact.organizationId().value()),
                        SourceEventId.of(fact.sourceEventId().value())));
    }

    @Override
    public Mono<AnalyticFact> update(AnalyticFact fact) {
        AnalyticFactRow row = rowMapper.toRow(fact, derivePeriod(fact.occurredAt()));
        return analyticFactRepository.save(row).map(rowMapper::toDomain);
    }

    @Override
    public Mono<AnalyticFact> findById(
            com.arka.reporting.domain.analyticfact.valueobject.OrganizationId organizationId,
            FactId factId) {
        return analyticFactRepository
                .findByOrganizationAndId(organizationId.value(), factId.value())
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<AnalyticFact> findBySourceEventId(
            com.arka.reporting.domain.analyticfact.valueobject.OrganizationId organizationId,
            SourceEventId sourceEventId) {
        return analyticFactRepository
                .findByOrganizationAndSourceEventId(organizationId.value(), sourceEventId.value())
                .map(rowMapper::toDomain);
    }

    @Override
    public Flux<FactSearchProjection> search(FactSearchFilter filter) {
        String sql = """
                SELECT fact_id,
                       source_event_id,
                       event_type,
                       fact_type,
                       fact_status,
                       period,
                       occurred_at,
                       updated_at
                FROM analytic_facts
                WHERE organization_id = :organizationId
                  AND (:eventType IS NULL OR event_type = :eventType)
                  AND (:factType IS NULL OR fact_type = :factType)
                  AND (:period IS NULL OR period = :period)
                  AND (:status IS NULL OR fact_status = :status)
                ORDER BY occurred_at DESC
                OFFSET :offset
                LIMIT :limit
                """;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("organizationId", filter.organizationId())
                .bind("offset", filter.offset())
                .bind("limit", filter.limit());

        spec = bindNullable(spec, "eventType", filter.eventType());
        spec = bindNullable(spec, "factType", filter.factType());
        spec = bindNullable(spec, "period", filter.period());
        spec = bindNullable(spec, "status", filter.status());

        return spec
                .map((row, metadata) -> new FactSearchProjection(
                        row.get("fact_id", String.class),
                        row.get("source_event_id", String.class),
                        row.get("event_type", String.class),
                        row.get("fact_type", String.class),
                        row.get("fact_status", String.class),
                        row.get("period", String.class),
                        row.get("occurred_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<Long> count(FactSearchFilter filter) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM analytic_facts
                WHERE organization_id = :organizationId
                  AND (:eventType IS NULL OR event_type = :eventType)
                  AND (:factType IS NULL OR fact_type = :factType)
                  AND (:period IS NULL OR period = :period)
                  AND (:status IS NULL OR fact_status = :status)
                """;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("organizationId", filter.organizationId());

        spec = bindNullable(spec, "eventType", filter.eventType());
        spec = bindNullable(spec, "factType", filter.factType());
        spec = bindNullable(spec, "period", filter.period());
        spec = bindNullable(spec, "status", filter.status());

        return spec
                .map((row, metadata) -> row.get("total", Long.class))
                .one()
                .defaultIfEmpty(0L);
    }

    @Override
    public Flux<AnalyticFact> findAppliedByOrganization(String organizationId) {
        return analyticFactRepository.findAppliedByOrganization(organizationId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<SalesProjection> upsertSalesProjection(
            String organizationId,
            String period,
            BigDecimal totalSalesDelta,
            BigDecimal paidAmountDelta,
            BigDecimal pendingAmountDelta,
            long confirmedOrdersDelta) {
        String sql = """
                INSERT INTO sales_projections (
                    projection_id,
                    organization_id,
                    period,
                    total_sales,
                    paid_amount,
                    pending_amount,
                    confirmed_orders,
                    average_ticket,
                    version,
                    created_at,
                    updated_at
                )
                VALUES (
                    :projectionId,
                    :organizationId,
                    :period,
                    :totalSales,
                    :paidAmount,
                    :pendingAmount,
                    :confirmedOrders,
                    CASE WHEN :confirmedOrders > 0 THEN :totalSales / :confirmedOrders ELSE 0 END,
                    0,
                    :now,
                    :now
                )
                ON CONFLICT (organization_id, period)
                DO UPDATE SET
                    total_sales = sales_projections.total_sales + EXCLUDED.total_sales,
                    paid_amount = sales_projections.paid_amount + EXCLUDED.paid_amount,
                    pending_amount = sales_projections.pending_amount + EXCLUDED.pending_amount,
                    confirmed_orders = sales_projections.confirmed_orders + EXCLUDED.confirmed_orders,
                    average_ticket = CASE
                        WHEN (sales_projections.confirmed_orders + EXCLUDED.confirmed_orders) > 0
                        THEN (sales_projections.total_sales + EXCLUDED.total_sales)
                                / (sales_projections.confirmed_orders + EXCLUDED.confirmed_orders)
                        ELSE 0
                    END,
                    version = sales_projections.version + 1,
                    updated_at = EXCLUDED.updated_at
                RETURNING *
                """;

        Instant now = Instant.now();
        return databaseClient.sql(sql)
                .bind("projectionId", UUID.randomUUID().toString())
                .bind("organizationId", organizationId)
                .bind("period", period)
                .bind("totalSales", normalizeDecimal(totalSalesDelta))
                .bind("paidAmount", normalizeDecimal(paidAmountDelta))
                .bind("pendingAmount", normalizeDecimal(pendingAmountDelta))
                .bind("confirmedOrders", confirmedOrdersDelta)
                .bind("now", now)
                .map((row, metadata) -> new SalesProjectionRow(
                        row.get("projection_id", String.class),
                        row.get("organization_id", String.class),
                        row.get("period", String.class),
                        row.get("total_sales", BigDecimal.class),
                        row.get("paid_amount", BigDecimal.class),
                        row.get("pending_amount", BigDecimal.class),
                        row.get("confirmed_orders", Long.class),
                        row.get("average_ticket", BigDecimal.class),
                        row.get("version", Long.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one()
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<ReplenishmentProjection> upsertReplenishmentProjection(
            String organizationId,
            String period,
            String sku,
            BigDecimal availableQty,
            BigDecimal reorderPoint,
            BigDecimal coverageDays,
            String riskLevel) {
        String sql = """
                INSERT INTO replenishment_projections (
                    projection_id,
                    organization_id,
                    period,
                    sku,
                    available_qty,
                    reorder_point,
                    coverage_days,
                    risk_level,
                    version,
                    created_at,
                    updated_at
                )
                VALUES (
                    :projectionId,
                    :organizationId,
                    :period,
                    :sku,
                    :availableQty,
                    :reorderPoint,
                    :coverageDays,
                    :riskLevel,
                    0,
                    :now,
                    :now
                )
                ON CONFLICT (organization_id, period, sku)
                DO UPDATE SET
                    available_qty = EXCLUDED.available_qty,
                    reorder_point = EXCLUDED.reorder_point,
                    coverage_days = EXCLUDED.coverage_days,
                    risk_level = EXCLUDED.risk_level,
                    version = replenishment_projections.version + 1,
                    updated_at = EXCLUDED.updated_at
                RETURNING *
                """;

        Instant now = Instant.now();
        return databaseClient.sql(sql)
                .bind("projectionId", UUID.randomUUID().toString())
                .bind("organizationId", organizationId)
                .bind("period", period)
                .bind("sku", sku)
                .bind("availableQty", normalizeDecimal(availableQty))
                .bind("reorderPoint", normalizeDecimal(reorderPoint))
                .bind("coverageDays", normalizeDecimal(coverageDays))
                .bind("riskLevel", riskLevel)
                .bind("now", now)
                .map((row, metadata) -> new ReplenishmentProjectionRow(
                        row.get("projection_id", String.class),
                        row.get("organization_id", String.class),
                        row.get("period", String.class),
                        row.get("sku", String.class),
                        row.get("available_qty", BigDecimal.class),
                        row.get("reorder_point", BigDecimal.class),
                        row.get("coverage_days", BigDecimal.class),
                        row.get("risk_level", String.class),
                        row.get("version", Long.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one()
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<OperationsKpiProjection> upsertOperationsKpiProjection(
            String organizationId,
            String period,
            String kpiName,
            BigDecimal kpiValue) {
        String sql = """
                INSERT INTO operations_kpi_projections (
                    projection_id,
                    organization_id,
                    period,
                    kpi_name,
                    kpi_value,
                    version,
                    created_at,
                    updated_at
                )
                VALUES (
                    :projectionId,
                    :organizationId,
                    :period,
                    :kpiName,
                    :kpiValue,
                    0,
                    :now,
                    :now
                )
                ON CONFLICT (organization_id, period, kpi_name)
                DO UPDATE SET
                    kpi_value = EXCLUDED.kpi_value,
                    version = operations_kpi_projections.version + 1,
                    updated_at = EXCLUDED.updated_at
                RETURNING *
                """;

        Instant now = Instant.now();
        return databaseClient.sql(sql)
                .bind("projectionId", UUID.randomUUID().toString())
                .bind("organizationId", organizationId)
                .bind("period", period)
                .bind("kpiName", kpiName)
                .bind("kpiValue", normalizeDecimal(kpiValue))
                .bind("now", now)
                .map((row, metadata) -> new OperationsKpiProjectionRow(
                        row.get("projection_id", String.class),
                        row.get("organization_id", String.class),
                        row.get("period", String.class),
                        row.get("kpi_name", String.class),
                        row.get("kpi_value", BigDecimal.class),
                        row.get("version", Long.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one()
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<SalesProjection> findSalesByPeriod(String organizationId, String period) {
        return salesProjectionRepository.findByOrganizationAndPeriod(organizationId, period).map(rowMapper::toDomain);
    }

    @Override
    public Flux<ReplenishmentProjection> findReplenishmentByPeriod(String organizationId, String period, String sku, int offset, int limit) {
        return replenishmentProjectionRepository
                .findByOrganizationAndPeriod(organizationId, period, emptyAsNull(sku), offset, limit)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Long> countReplenishmentByPeriod(String organizationId, String period, String sku) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM replenishment_projections
                WHERE organization_id = :organizationId
                  AND period = :period
                  AND (:sku IS NULL OR sku = :sku)
                """;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("organizationId", organizationId)
                .bind("period", period);
        spec = bindNullable(spec, "sku", sku);
        return spec
                .map((row, metadata) -> row.get("total", Long.class))
                .one()
                .defaultIfEmpty(0L);
    }

    @Override
    public Flux<OperationsKpiProjection> findOperationsKpisByPeriod(String organizationId, String period) {
        return operationsKpiProjectionRepository.findByOrganizationAndPeriod(organizationId, period).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Void> clearProjectionsByOrganization(String organizationId) {
        return databaseClient.sql("DELETE FROM sales_projections WHERE organization_id = :organizationId")
                .bind("organizationId", organizationId)
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("DELETE FROM replenishment_projections WHERE organization_id = :organizationId")
                        .bind("organizationId", organizationId)
                        .fetch()
                        .rowsUpdated()
                        .then())
                .then(databaseClient.sql("DELETE FROM operations_kpi_projections WHERE organization_id = :organizationId")
                        .bind("organizationId", organizationId)
                        .fetch()
                        .rowsUpdated()
                        .then());
    }

    @Override
    public Mono<Void> clearProjectionsByOrganizationAndPeriod(String organizationId, String period) {
        return databaseClient.sql("DELETE FROM sales_projections WHERE organization_id = :organizationId AND period = :period")
                .bind("organizationId", organizationId)
                .bind("period", period)
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("DELETE FROM replenishment_projections WHERE organization_id = :organizationId AND period = :period")
                        .bind("organizationId", organizationId)
                        .bind("period", period)
                        .fetch()
                        .rowsUpdated()
                        .then())
                .then(databaseClient.sql("DELETE FROM operations_kpi_projections WHERE organization_id = :organizationId AND period = :period")
                        .bind("organizationId", organizationId)
                        .bind("period", period)
                        .fetch()
                        .rowsUpdated()
                        .then());
    }

    @Override
    public Mono<WeeklyReportExecution> create(WeeklyReportExecution execution) {
        return entityTemplate
                .insert(rowMapper.toRow(execution))
                .map(rowMapper::toDomain)
                .onErrorResume(this::isDuplicate, throwable -> findByWeekAndType(
                        com.arka.reporting.domain.weeklyreportexecution.valueobject.OrganizationId.of(execution.organizationId().value()),
                        WeekId.of(execution.weekId().value()),
                        execution.reportType().name()));
    }

    @Override
    public Mono<WeeklyReportExecution> update(WeeklyReportExecution execution) {
        long expectedVersion = execution.version();
        long nextVersion = expectedVersion + 1;
        return weeklyReportExecutionRepository
                .updateOptimistic(
                        execution.organizationId().value(),
                        execution.executionId().value(),
                        execution.status().name(),
                        emptyAsNull(execution.errorCode()),
                        emptyAsNull(execution.errorMessage()),
                        emptyAsNull(execution.completionArtifactRef()),
                        expectedVersion,
                        nextVersion,
                        execution.startedAt(),
                        execution.completedAt(),
                        execution.updatedAt())
                .flatMap(rowsUpdated -> {
                    if (rowsUpdated == null || rowsUpdated == 0) {
                        return Mono.error(new OptimisticLockingFailureException());
                    }
                    return findById(execution.organizationId(), execution.executionId());
                });
    }

    @Override
    public Mono<WeeklyReportExecution> findById(
            com.arka.reporting.domain.weeklyreportexecution.valueobject.OrganizationId organizationId,
            ExecutionId executionId) {
        return weeklyReportExecutionRepository
                .findByOrganizationAndId(organizationId.value(), executionId.value())
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<WeeklyReportExecution> findByWeekAndType(
            com.arka.reporting.domain.weeklyreportexecution.valueobject.OrganizationId organizationId,
            WeekId weekId,
            String reportType) {
        return weeklyReportExecutionRepository
                .findByOrganizationWeekAndType(organizationId.value(), weekId.value(), reportType)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsRunningRebuild(
            com.arka.reporting.domain.weeklyreportexecution.valueobject.OrganizationId organizationId) {
        return weeklyReportExecutionRepository.existsRunningRebuild(organizationId.value()).defaultIfEmpty(false);
    }

    @Override
    public Mono<ReportArtifact> create(ReportArtifact artifact) {
        return entityTemplate
                .insert(rowMapper.toRow(artifact))
                .map(rowMapper::toDomain)
                .onErrorResume(
                        this::isDuplicate,
                        throwable -> reportArtifactRepository
                                .findByUnique(
                                        artifact.organizationId(),
                                        artifact.weekId(),
                                        artifact.reportType(),
                                        artifact.format())
                                .map(rowMapper::toDomain));
    }

    @Override
    public Mono<ReportArtifact> findById(String organizationId, String artifactId) {
        return reportArtifactRepository.findByOrganizationAndId(organizationId, artifactId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<ReportArtifact> findByExecutionId(String organizationId, String executionId) {
        return reportArtifactRepository.findByExecutionId(organizationId, executionId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<ReportArtifact> findByWeekAndType(String organizationId, String weekId, String reportType, int offset, int limit) {
        return reportArtifactRepository
                .findByWeekAndType(organizationId, weekId, reportType, offset, limit)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Long> countByWeekAndType(String organizationId, String weekId, String reportType) {
        return reportArtifactRepository.countByWeekAndType(organizationId, weekId, reportType).defaultIfEmpty(0L);
    }

    @Override
    public Mono<ConsumerCheckpoint> upsert(
            String organizationId,
            String consumerName,
            String topic,
            int partition,
            long currentOffset,
            long latestOffset) {
        String sql = """
                INSERT INTO consumer_checkpoints (
                    checkpoint_id,
                    organization_id,
                    consumer_name,
                    topic,
                    partition,
                    current_offset,
                    latest_offset,
                    lag,
                    updated_at
                )
                VALUES (
                    :checkpointId,
                    :organizationId,
                    :consumerName,
                    :topic,
                    :partition,
                    :currentOffset,
                    :latestOffset,
                    GREATEST(:latestOffset - :currentOffset, 0),
                    :now
                )
                ON CONFLICT (organization_id, consumer_name, topic, partition)
                DO UPDATE SET
                    current_offset = EXCLUDED.current_offset,
                    latest_offset = EXCLUDED.latest_offset,
                    lag = EXCLUDED.lag,
                    updated_at = EXCLUDED.updated_at
                RETURNING *
                """;

        Instant now = Instant.now();
        return databaseClient.sql(sql)
                .bind("checkpointId", UUID.randomUUID().toString())
                .bind("organizationId", organizationId)
                .bind("consumerName", consumerName)
                .bind("topic", topic)
                .bind("partition", partition)
                .bind("currentOffset", currentOffset)
                .bind("latestOffset", latestOffset)
                .bind("now", now)
                .map((row, metadata) -> rowMapper.toDomain(new com.arka.reporting.infrastructure.adapter.out.persistence.entity.ConsumerCheckpointRow(
                        row.get("checkpoint_id", String.class),
                        row.get("organization_id", String.class),
                        row.get("consumer_name", String.class),
                        row.get("topic", String.class),
                        row.get("partition", Integer.class),
                        row.get("current_offset", Long.class),
                        row.get("latest_offset", Long.class),
                        row.get("lag", Long.class),
                        row.get("updated_at", Instant.class))))
                .one();
    }

    @Override
    public Mono<Long> maxLagByOrganization(String organizationId) {
        return consumerCheckpointRepository.maxLagByOrganization(organizationId).defaultIfEmpty(0L);
    }

    @Override
    public Mono<ReportingMetricsProjection> metrics(String organizationId, String period) {
        String resolvedPeriod = emptyAsNull(period);

        Mono<String> periodMono = resolvedPeriod == null
                ? databaseClient
                        .sql("SELECT MAX(period) AS period FROM sales_projections WHERE organization_id = :organizationId")
                        .bind("organizationId", organizationId)
                        .map((row, metadata) -> {
                            String value = row.get("period", String.class);
                            return value == null ? "" : value;
                        })
                        .one()
                        .defaultIfEmpty("")
                : Mono.just(resolvedPeriod);

        return periodMono.flatMap(resolved -> {
            Mono<SalesProjectionRow> salesRowMono = (resolved == null || resolved.isBlank())
                    ? Mono.empty()
                    : salesProjectionRepository.findByOrganizationAndPeriod(organizationId, resolved);

            Mono<BigDecimal> weeklySalesTotalMono = salesRowMono
                    .map(SalesProjectionRow::totalSales)
                    .defaultIfEmpty(BigDecimal.ZERO);

            Mono<BigDecimal> weeklyCollectionRateMono = salesRowMono
                    .map(row -> {
                        BigDecimal total = defaultDecimal(row.totalSales());
                        if (total.compareTo(BigDecimal.ZERO) == 0) {
                            return BigDecimal.ZERO;
                        }
                        return defaultDecimal(row.paidAmount())
                                .multiply(BigDecimal.valueOf(100))
                                .divide(total, 2, RoundingMode.HALF_UP);
                    })
                    .defaultIfEmpty(BigDecimal.ZERO);

            String filterPeriod = resolved == null || resolved.isBlank() ? null : resolved;

            DatabaseClient.GenericExecuteSpec riskSpec = databaseClient
                    .sql("""
                            SELECT COUNT(*) AS total
                            FROM replenishment_projections
                            WHERE organization_id = :organizationId
                              AND (:period IS NULL OR period = :period)
                              AND risk_level IN ('HIGH', 'CRITICAL')
                            """)
                    .bind("organizationId", organizationId);
            riskSpec = filterPeriod == null
                    ? riskSpec.bindNull("period", String.class)
                    : riskSpec.bind("period", filterPeriod);

            DatabaseClient.GenericExecuteSpec notificationSpec = databaseClient
                    .sql("""
                            SELECT COALESCE(AVG(kpi_value), 0) AS avg_value
                            FROM operations_kpi_projections
                            WHERE organization_id = :organizationId
                              AND (:period IS NULL OR period = :period)
                              AND kpi_name = 'notification_effectiveness'
                            """)
                    .bind("organizationId", organizationId);
            notificationSpec = filterPeriod == null
                    ? notificationSpec.bindNull("period", String.class)
                    : notificationSpec.bind("period", filterPeriod);

            Mono<Long> highRiskMono = riskSpec
                    .map((row, metadata) -> row.get("total", Long.class))
                    .one()
                    .defaultIfEmpty(0L);

            Mono<BigDecimal> notificationEffectivenessMono = notificationSpec
                    .map((row, metadata) -> defaultDecimal(row.get("avg_value", BigDecimal.class)))
                    .one()
                    .defaultIfEmpty(BigDecimal.ZERO);

            Mono<Long> consumerLagMono = maxLagByOrganization(organizationId);

            return Mono.zip(
                            weeklySalesTotalMono,
                            weeklyCollectionRateMono,
                            highRiskMono,
                            notificationEffectivenessMono,
                            consumerLagMono)
                    .map(tuple -> new ReportingMetricsProjection(
                            defaultDecimal(tuple.getT1()),
                            defaultDecimal(tuple.getT2()),
                            tuple.getT3(),
                            defaultDecimal(tuple.getT4()),
                            tuple.getT5()));
        });
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
                .onErrorResume(this::isDuplicate, throwable -> Mono.empty());
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
    public Mono<Void> record(ReportingAuditEntry entry) {
        return entityTemplate.insert(rowMapper.toRow(entry)).then();
    }

    @Override
    public Mono<ReportingAuditEntry> findByIdempotency(String organizationId, String actionType, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.empty();
        }
        return reportingAuditRepository
                .findByIdempotency(organizationId, actionType, idempotencyKey)
                .map(rowMapper::toDomain);
    }

    @Override
    public Flux<ReportingAuditEntry> findByTarget(String organizationId, String targetType, String targetId, int offset, int limit) {
        return reportingAuditRepository
                .findByTarget(organizationId, emptyAsNull(targetType), emptyAsNull(targetId), offset, limit)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Long> countByTarget(String organizationId, String targetType, String targetId) {
        return reportingAuditRepository.countByTarget(organizationId, emptyAsNull(targetType), emptyAsNull(targetId));
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

    private BigDecimal normalizeDecimal(BigDecimal value) {
        return defaultDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String emptyAsNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isDuplicate(Throwable throwable) {
        return throwable instanceof DuplicateKeyException || throwable instanceof DataIntegrityViolationException;
    }

    private String derivePeriod(Instant occurredAt) {
        Instant effective = occurredAt == null ? Instant.now() : occurredAt;
        WeekFields weekFields = WeekFields.ISO;
        int year = effective.atZone(ZoneOffset.UTC).get(weekFields.weekBasedYear());
        int week = effective.atZone(ZoneOffset.UTC).get(weekFields.weekOfWeekBasedYear());
        return String.format("%04d-W%02d", year, week);
    }
}
