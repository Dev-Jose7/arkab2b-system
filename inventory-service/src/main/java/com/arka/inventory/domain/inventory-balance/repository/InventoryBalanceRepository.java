package com.arka.inventory.domain.inventorybalance.repository;

import com.arka.inventory.domain.inventorybalance.aggregate.InventoryBalance;
import java.util.Optional;

public interface InventoryBalanceRepository {

    Optional<InventoryBalance> findByStockItemId(String stockItemId);

    InventoryBalance save(InventoryBalance inventoryBalance);
}
