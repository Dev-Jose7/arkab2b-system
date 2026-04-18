package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.catalogoffer.entity.PriceSchedule;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceScheduleJobStatus;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PriceSchedulePersistencePort {

    Mono<PriceSchedule> create(PriceSchedule schedule);

    Flux<PriceSchedule> findPending(Instant executeBefore, int limit);

    Mono<Void> markStatus(String scheduleId, PriceScheduleJobStatus jobStatus, String errorMessage, Instant updatedAt);
}
