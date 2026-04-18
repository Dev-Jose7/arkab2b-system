package com.arka.catalog.infrastructure.adapter.out.persistence.repository;

import com.arka.catalog.infrastructure.adapter.out.persistence.entity.ProcessedEventRow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactiveProcessedEventRepository extends ReactiveCrudRepository<ProcessedEventRow, String> {
}
