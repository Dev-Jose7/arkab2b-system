package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.application.mapper.result.InventoryResultMapper;
import com.arka.inventory.application.result.StockItemResult;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveStockItemRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class InventoryBacklogReadService {

    private final ReactiveStockItemRepository stockItemRepository;
    private final InventoryRowMapper rowMapper;
    private final InventoryResultMapper resultMapper;

    public InventoryBacklogReadService(
            ReactiveStockItemRepository stockItemRepository,
            InventoryRowMapper rowMapper,
            InventoryResultMapper resultMapper) {
        this.stockItemRepository = stockItemRepository;
        this.rowMapper = rowMapper;
        this.resultMapper = resultMapper;
    }

    public Flux<StockItemResult> listLowStockWithThreshold(
            String organizationId,
            String warehouseId,
            Integer threshold) {
        int safeThreshold = threshold == null ? 0 : Math.max(0, threshold);
        return stockItemRepository
                .findLowStockByOrganizationAndWarehouseWithThreshold(organizationId, warehouseId, safeThreshold)
                .map(rowMapper::toDomain)
                .map(resultMapper::toResult);
    }
}
