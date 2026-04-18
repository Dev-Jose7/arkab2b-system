package com.arka.reporting.application.port.out.persistence;

import com.arka.reporting.domain.weeklyreportexecution.entity.OperationsKpiProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReplenishmentProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.SalesProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectionPersistencePort {

    Mono<SalesProjection> upsertSalesProjection(
            String tenantId,
            String period,
            java.math.BigDecimal totalSalesDelta,
            java.math.BigDecimal paidAmountDelta,
            java.math.BigDecimal pendingAmountDelta,
            long confirmedOrdersDelta);

    Mono<ReplenishmentProjection> upsertReplenishmentProjection(
            String tenantId,
            String period,
            String sku,
            java.math.BigDecimal availableQty,
            java.math.BigDecimal reorderPoint,
            java.math.BigDecimal coverageDays,
            String riskLevel);

    Mono<OperationsKpiProjection> upsertOperationsKpiProjection(
            String tenantId,
            String period,
            String kpiName,
            java.math.BigDecimal kpiValue);

    Mono<SalesProjection> findSalesByPeriod(String tenantId, String period);

    Flux<ReplenishmentProjection> findReplenishmentByPeriod(String tenantId, String period, String sku, int offset, int limit);

    Mono<Long> countReplenishmentByPeriod(String tenantId, String period, String sku);

    Flux<OperationsKpiProjection> findOperationsKpisByPeriod(String tenantId, String period);

    Mono<Void> clearProjectionsByTenant(String tenantId);

    Mono<Void> clearProjectionsByTenantAndPeriod(String tenantId, String period);
}
