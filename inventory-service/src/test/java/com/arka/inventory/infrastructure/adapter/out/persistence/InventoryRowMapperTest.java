package com.arka.inventory.infrastructure.adapter.out.persistence;

import com.arka.inventory.domain.inventorybalance.entity.StockItem;
import com.arka.inventory.infrastructure.adapter.out.persistence.entity.StockItemRow;
import com.arka.inventory.infrastructure.adapter.out.persistence.mapper.InventoryRowMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryRowMapperTest {

    @Test
    void mapsStockItemRowToDomainAndBack() {
        InventoryRowMapper mapper = new InventoryRowMapper();

        StockItemRow row = new StockItemRow(
                "stock-1",
                "tenant-1",
                "wh-1",
                "SKU-1",
                10,
                3,
                2,
                1,
                "ACTIVE",
                4L,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T01:00:00Z"));

        StockItem domain = mapper.toDomain(row);
        StockItemRow mapped = mapper.toRow(domain);

        assertEquals("stock-1", domain.stockItemId());
        assertEquals(7, domain.availableQty());
        assertEquals("ACTIVE", domain.status().name());
        assertEquals(row.stockItemId(), mapped.stockItemId());
        assertEquals(row.version(), mapped.version());
    }
}
