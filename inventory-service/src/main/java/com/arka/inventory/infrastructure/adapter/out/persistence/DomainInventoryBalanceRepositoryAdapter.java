package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.domain.inventorybalance.aggregate.InventoryBalance;
import com.arka.inventory.domain.inventorybalance.repository.InventoryBalanceRepository;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import com.arka.inventory.infrastructure.adapter.out.persistence.repository.ReactiveStockItemRepository;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DomainInventoryBalanceRepositoryAdapter implements InventoryBalanceRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final ReactiveStockItemRepository stockItemRepository;
    private final InventoryRowMapper rowMapper;

    public DomainInventoryBalanceRepositoryAdapter(
            ReactiveStockItemRepository stockItemRepository,
            InventoryRowMapper rowMapper) {
        this.stockItemRepository = stockItemRepository;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<InventoryBalance> findByStockItemId(String stockItemId) {
        return stockItemRepository.findById(stockItemId).map(rowMapper::toDomain).map(InventoryBalance::from).blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public InventoryBalance save(InventoryBalance inventoryBalance) {
        var persisted = stockItemRepository
                .save(rowMapper.toRow(inventoryBalance.stockItem()))
                .map(rowMapper::toDomain)
                .map(InventoryBalance::from)
                .block(BLOCK_TIMEOUT);
        if (persisted == null) {
            throw new IllegalStateException("InventoryBalance persistence returned empty result");
        }
        return persisted;
    }
}
