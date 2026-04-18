package com.arka.order.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.domain.order.entity.OrderLine;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.PurchaseOrderPersistenceMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PurchaseOrderPersistenceMapperTest {

    private final PurchaseOrderPersistenceMapper mapper = new PurchaseOrderPersistenceMapper();

    @Test
    void shouldMapOrderAggregateToPersistenceEntity() {
        Instant now = Instant.parse("2026-04-14T12:00:00Z");
        Order order = Order.createFromValidatedCart(
                "tenant-1",
                "org-1",
                "user-1",
                "cart-1",
                "corr-1",
                "addr-1",
                "US",
                1L,
                "USD",
                List.of(new OrderLine(
                        UUID.randomUUID().toString(),
                        "PENDING_ORDER_ID",
                        "tenant-1",
                        "org-1",
                        "variant-1",
                        "SKU-1",
                        2,
                        new BigDecimal("10.00"),
                        "USD",
                        "res-1",
                        true,
                        null,
                        now,
                        now)),
                now);

        var entity = mapper.toEntity(order);
        var lineEntities = mapper.toOrderLineEntities(order);

        assertEquals(order.orderId(), entity.orderId());
        assertEquals(order.status().name(), entity.status());
        assertEquals(1, lineEntities.size());
        assertEquals(order.orderId(), lineEntities.getFirst().orderId());
        assertEquals(order.totalAmount(), lineEntities.getFirst().lineTotal());
    }
}
