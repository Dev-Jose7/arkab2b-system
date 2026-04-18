package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.PriceScheduleRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactivePriceScheduleRepository extends ReactiveCrudRepository<PriceScheduleRow, String> {

    @Query("""
            SELECT *
            FROM price_schedules
            WHERE job_status = 'PENDING'
              AND execute_after <= :executeBefore
            ORDER BY execute_after
            LIMIT :limit
            """)
    Flux<PriceScheduleRow> findPending(Instant executeBefore, int limit);

    @Query("""
            UPDATE price_schedules
            SET job_status = :jobStatus,
                error_message = :errorMessage,
                updated_at = :updatedAt
            WHERE schedule_id = :scheduleId
            """)
    Mono<Integer> markStatus(String scheduleId, String jobStatus, String errorMessage, Instant updatedAt);
}
