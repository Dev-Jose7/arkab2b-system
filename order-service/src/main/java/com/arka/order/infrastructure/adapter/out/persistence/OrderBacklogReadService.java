package com.arka.order.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrderBacklogReadService {

    private static final Pattern WEEK_ID_PATTERN = Pattern.compile("^(\\d{4})-W(\\d{2})$");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");

    private final DatabaseClient databaseClient;

    public OrderBacklogReadService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<WeeklySalesSummarySnapshot> loadWeeklySalesSummary(String organizationId, String weekId, int limit) {
        WeekRange range = resolveWeekRange(weekId);
        int safeLimit = Math.max(1, Math.min(limit, 20));

        Mono<TotalsSnapshot> totalsMono = databaseClient.sql("""
                        SELECT COALESCE(SUM(total_amount), 0) AS total_sales,
                               COUNT(*) AS total_orders
                        FROM purchase_orders
                        WHERE organization_id = :organizationId
                          AND status <> 'CANCELLED'
                          AND created_at >= :fromAt
                          AND created_at < :toAt
                        """)
                .bind("organizationId", organizationId)
                .bind("fromAt", range.from())
                .bind("toAt", range.to())
                .map((row, metadata) -> new TotalsSnapshot(
                        decimal(row.get("total_sales")),
                        number(row.get("total_orders")).longValue()))
                .one()
                .defaultIfEmpty(new TotalsSnapshot(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), 0L));

        Mono<List<TopProductSnapshot>> topProductsMono = databaseClient.sql("""
                        SELECT ol.sku,
                               COALESCE(SUM(ol.qty), 0) AS total_qty,
                               COALESCE(SUM(ol.line_total), 0) AS total_sales
                        FROM order_lines ol
                        JOIN purchase_orders po ON po.order_id = ol.order_id
                        WHERE po.organization_id = :organizationId
                          AND po.status <> 'CANCELLED'
                          AND po.created_at >= :fromAt
                          AND po.created_at < :toAt
                        GROUP BY ol.sku
                        ORDER BY total_qty DESC, total_sales DESC, ol.sku ASC
                        LIMIT %d
                        """.formatted(safeLimit))
                .bind("organizationId", organizationId)
                .bind("fromAt", range.from())
                .bind("toAt", range.to())
                .map((row, metadata) -> new TopProductSnapshot(
                        text(row.get("sku")),
                        number(row.get("total_qty")).longValue(),
                        decimal(row.get("total_sales"))))
                .all()
                .collectList();

        Mono<List<FrequentCustomerSnapshot>> frequentCustomersMono = databaseClient.sql("""
                        SELECT user_id,
                               COUNT(*) AS order_count,
                               COALESCE(SUM(total_amount), 0) AS total_spent
                        FROM purchase_orders
                        WHERE organization_id = :organizationId
                          AND status <> 'CANCELLED'
                          AND created_at >= :fromAt
                          AND created_at < :toAt
                        GROUP BY user_id
                        ORDER BY order_count DESC, total_spent DESC, user_id ASC
                        LIMIT %d
                        """.formatted(safeLimit))
                .bind("organizationId", organizationId)
                .bind("fromAt", range.from())
                .bind("toAt", range.to())
                .map((row, metadata) -> new FrequentCustomerSnapshot(
                        text(row.get("user_id")),
                        number(row.get("order_count")).longValue(),
                        decimal(row.get("total_spent"))))
                .all()
                .collectList();

        return Mono.zip(totalsMono, topProductsMono, frequentCustomersMono)
                .map(tuple -> new WeeklySalesSummarySnapshot(
                        organizationId,
                        range.weekId(),
                        range.from(),
                        range.to(),
                        tuple.getT1().totalSales(),
                        tuple.getT1().totalOrders(),
                        tuple.getT2(),
                        tuple.getT3()));
    }

    public Flux<AbandonedCartSnapshot> listAbandonedCarts(String organizationId, int inactiveHours, int limit) {
        Instant cutoff = Instant.now().minusSeconds(Math.max(1, inactiveHours) * 3600L);
        int safeLimit = Math.max(1, Math.min(limit, 100));

        return databaseClient.sql("""
                        SELECT c.cart_id,
                               c.organization_id,
                               c.user_id,
                               c.status,
                               c.created_at,
                               c.updated_at
                        FROM carts c
                        WHERE c.organization_id = :organizationId
                          AND (
                                c.status = 'ABANDONED'
                                OR (c.status IN ('ACTIVE', 'CHECKOUT_IN_PROGRESS') AND c.updated_at < :cutoff)
                              )
                        ORDER BY c.updated_at ASC
                        LIMIT %d
                        """.formatted(safeLimit))
                .bind("organizationId", organizationId)
                .bind("cutoff", cutoff)
                .map((row, metadata) -> new AbandonedCartBase(
                        text(row.get("cart_id")),
                        text(row.get("organization_id")),
                        text(row.get("user_id")),
                        text(row.get("status")),
                        instant(row.get("created_at")),
                        instant(row.get("updated_at"))))
                .all()
                .concatMap(base -> loadCartItems(base.cartId())
                        .collectList()
                        .map(items -> new AbandonedCartSnapshot(
                                base.cartId(),
                                base.organizationId(),
                                base.userId(),
                                base.status(),
                                !"ABANDONED".equalsIgnoreCase(base.status()),
                                base.createdAt(),
                                base.updatedAt(),
                                items)));
    }

    public Mono<AbandonedCartSnapshot> findAbandonedCart(String organizationId, String cartId, int inactiveHours) {
        return listAbandonedCarts(organizationId, inactiveHours, 200)
                .filter(cart -> cart.cartId().equals(cartId))
                .next();
    }

    private Flux<AbandonedCartItemSnapshot> loadCartItems(String cartId) {
        return databaseClient.sql("""
                        SELECT cart_item_id,
                               variant_id,
                               sku,
                               qty,
                               unit_price,
                               currency
                        FROM cart_items
                        WHERE cart_id = :cartId
                        ORDER BY created_at ASC
                        """)
                .bind("cartId", cartId)
                .map((row, metadata) -> {
                    int qty = number(row.get("qty")).intValue();
                    BigDecimal unitPrice = decimal(row.get("unit_price"));
                    return new AbandonedCartItemSnapshot(
                            text(row.get("cart_item_id")),
                            text(row.get("variant_id")),
                            text(row.get("sku")),
                            qty,
                            unitPrice,
                            unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP),
                            text(row.get("currency")));
                })
                .all();
    }

    private WeekRange resolveWeekRange(String weekId) {
        Matcher matcher = WEEK_ID_PATTERN.matcher(weekId == null ? "" : weekId.trim().toUpperCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("weekId debe tener formato YYYY-Www");
        }
        int year = Integer.parseInt(matcher.group(1));
        int week = Integer.parseInt(matcher.group(2));
        WeekFields weekFields = WeekFields.ISO;
        LocalDate start = LocalDate.of(year, 1, 4)
                .with(weekFields.weekBasedYear(), year)
                .with(weekFields.weekOfWeekBasedYear(), week)
                .with(weekFields.dayOfWeek(), 1);
        LocalDate end = start.plusDays(7);
        return new WeekRange(
                "%04d-W%02d".formatted(year, week),
                start.atStartOfDay(BUSINESS_ZONE).toInstant(),
                end.atStartOfDay(BUSINESS_ZONE).toInstant());
    }

    private BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
    }

    private Number number(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Instant instant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        return value == null ? Instant.EPOCH : Instant.parse(String.valueOf(value));
    }

    private record WeekRange(String weekId, Instant from, Instant to) {
    }

    private record TotalsSnapshot(BigDecimal totalSales, long totalOrders) {
    }

    private record AbandonedCartBase(
            String cartId,
            String organizationId,
            String userId,
            String status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record WeeklySalesSummarySnapshot(
            String organizationId,
            String weekId,
            Instant from,
            Instant to,
            BigDecimal totalSales,
            long totalOrders,
            List<TopProductSnapshot> topProducts,
            List<FrequentCustomerSnapshot> frequentCustomers) {
    }

    public record TopProductSnapshot(
            String sku,
            long totalQty,
            BigDecimal totalSales) {
    }

    public record FrequentCustomerSnapshot(
            String userId,
            long orderCount,
            BigDecimal totalSpent) {
    }

    public record AbandonedCartSnapshot(
            String cartId,
            String organizationId,
            String userId,
            String status,
            boolean inferredAbandoned,
            Instant createdAt,
            Instant updatedAt,
            List<AbandonedCartItemSnapshot> items) {
    }

    public record AbandonedCartItemSnapshot(
            String cartItemId,
            String variantId,
            String sku,
            int qty,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            String currency) {
    }
}
