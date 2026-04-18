package com.arka.catalog.infrastructure.adapter.out.persistence;

import com.arka.catalog.application.port.out.audit.CatalogAuditEntry;
import com.arka.catalog.application.port.out.audit.CatalogAuditPort;
import com.arka.catalog.application.port.out.persistence.CatalogOfferPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogPricePersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogProductPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogReadPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogSearchFilter;
import com.arka.catalog.application.port.out.persistence.CatalogSearchProjection;
import com.arka.catalog.application.port.out.persistence.CatalogTaxonomyPersistencePort;
import com.arka.catalog.application.port.out.persistence.CatalogVariantPersistencePort;
import com.arka.catalog.application.port.out.persistence.OutboxPersistencePort;
import com.arka.catalog.application.port.out.persistence.OutboxRelayPort;
import com.arka.catalog.application.port.out.persistence.PendingOutboxEvent;
import com.arka.catalog.application.port.out.persistence.PriceSchedulePersistencePort;
import com.arka.catalog.domain.catalogoffer.aggregate.CatalogOffer;
import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.entity.PriceSchedule;
import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.ProductTag;
import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceScheduleJobStatus;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.valueobject.OfferId;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.OrganizationId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import com.arka.catalog.domain.shared.event.DomainEvent;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.CatalogAuditRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import com.arka.catalog.infrastructure.adapter.out.persistence.mapper.CatalogRowMapper;
import com.arka.catalog.infrastructure.adapter.out.persistence.mapper.OutboxRowMapper;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveBrandRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveCatalogAuditRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveCategoryRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactivePriceRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactivePriceScheduleRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveProductRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveProductTagRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveVariantAttributeRepository;
import com.arka.catalog.infrastructure.adapter.out.persistence.repository.ReactiveVariantRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CatalogR2dbcPersistenceAdapter
        implements CatalogTaxonomyPersistencePort,
                CatalogProductPersistencePort,
                CatalogVariantPersistencePort,
                CatalogPricePersistencePort,
                CatalogOfferPersistencePort,
                PriceSchedulePersistencePort,
                CatalogAuditPort,
                OutboxPersistencePort,
                OutboxRelayPort,
                CatalogReadPersistencePort {

    private final ReactiveBrandRepository brandRepository;
    private final ReactiveCategoryRepository categoryRepository;
    private final ReactiveProductRepository productRepository;
    private final ReactiveProductTagRepository productTagRepository;
    private final ReactiveVariantRepository variantRepository;
    private final ReactiveVariantAttributeRepository variantAttributeRepository;
    private final ReactivePriceRepository priceRepository;
    private final ReactivePriceScheduleRepository priceScheduleRepository;
    private final ReactiveCatalogAuditRepository catalogAuditRepository;
    private final ReactiveOutboxEventRepository outboxEventRepository;
    private final CatalogRowMapper rowMapper;
    private final OutboxRowMapper outboxRowMapper;
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate entityTemplate;

    public CatalogR2dbcPersistenceAdapter(
            ReactiveBrandRepository brandRepository,
            ReactiveCategoryRepository categoryRepository,
            ReactiveProductRepository productRepository,
            ReactiveProductTagRepository productTagRepository,
            ReactiveVariantRepository variantRepository,
            ReactiveVariantAttributeRepository variantAttributeRepository,
            ReactivePriceRepository priceRepository,
            ReactivePriceScheduleRepository priceScheduleRepository,
            ReactiveCatalogAuditRepository catalogAuditRepository,
            ReactiveOutboxEventRepository outboxEventRepository,
            CatalogRowMapper rowMapper,
            OutboxRowMapper outboxRowMapper,
            DatabaseClient databaseClient,
            R2dbcEntityTemplate entityTemplate) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productTagRepository = productTagRepository;
        this.variantRepository = variantRepository;
        this.variantAttributeRepository = variantAttributeRepository;
        this.priceRepository = priceRepository;
        this.priceScheduleRepository = priceScheduleRepository;
        this.catalogAuditRepository = catalogAuditRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.rowMapper = rowMapper;
        this.outboxRowMapper = outboxRowMapper;
        this.databaseClient = databaseClient;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Boolean> isBrandActive(String organizationId, String brandId) {
        return brandRepository.findActive(organizationId, brandId).hasElement();
    }

    @Override
    public Mono<Boolean> isCategoryActive(String organizationId, String categoryId) {
        return categoryRepository.findActive(organizationId, categoryId).hasElement();
    }

    @Override
    public Mono<Product> create(Product product, Iterable<ProductTag> tags) {
        return entityTemplate.insert(rowMapper.toRow(product))
                .then(replaceProductTags(product.organizationId().value(), product.productId().value(), tags))
                .thenReturn(product);
    }

    @Override
    public Mono<Product> update(Product product, Iterable<ProductTag> tags) {
        Mono<Void> tagsUpdate = tags == null
                ? Mono.empty()
                : replaceProductTags(product.organizationId().value(), product.productId().value(), tags);
        return productRepository.save(rowMapper.toRow(product)).then(tagsUpdate).thenReturn(product);
    }

    @Override
    public Mono<Product> findById(OrganizationId organizationId, ProductId productId) {
        return productRepository.findByOrganizationAndId(organizationId.value(), productId.value()).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByProductCode(OrganizationId organizationId, String productCode, String excludingProductId) {
        return productRepository.existsByProductCode(organizationId.value(), productCode, excludingProductId).defaultIfEmpty(false);
    }

    @Override
    public Flux<ProductTag> findTags(OrganizationId organizationId, ProductId productId) {
        return productTagRepository.findByOrganizationAndProduct(organizationId.value(), productId.value()).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Variant> create(Variant variant, Iterable<VariantAttribute> attributes) {
        return entityTemplate.insert(rowMapper.toRow(variant))
                .then(replaceVariantAttributes(variant.organizationId().value(), variant.variantId().value(), attributes))
                .thenReturn(variant);
    }

    @Override
    public Mono<Variant> update(Variant variant) {
        return variantRepository.save(rowMapper.toRow(variant)).thenReturn(variant);
    }

    @Override
    public Mono<Variant> findById(OrganizationId organizationId, VariantId variantId) {
        return variantRepository.findByOrganizationAndId(organizationId.value(), variantId.value()).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Variant> findBySku(OrganizationId organizationId, String sku) {
        return variantRepository.findByOrganizationAndSku(organizationId.value(), sku).map(rowMapper::toDomain);
    }

    @Override
    public Flux<Variant> findByProductId(OrganizationId organizationId, ProductId productId) {
        return variantRepository.findByOrganizationAndProduct(organizationId.value(), productId.value()).map(rowMapper::toDomain);
    }

    @Override
    public Flux<VariantAttribute> findAttributes(OrganizationId organizationId, VariantId variantId) {
        return variantAttributeRepository
                .findByOrganizationAndVariant(organizationId.value(), variantId.value())
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Void> replaceAttributes(OrganizationId organizationId, VariantId variantId, Iterable<VariantAttribute> attributes) {
        return replaceVariantAttributes(organizationId.value(), variantId.value(), attributes);
    }

    @Override
    public Mono<Boolean> existsSellableSku(OrganizationId organizationId, String sku, String excludingVariantId) {
        return variantRepository.existsSellableSku(organizationId.value(), sku, excludingVariantId).defaultIfEmpty(false);
    }

    @Override
    public Mono<Price> create(Price price) {
        return entityTemplate.insert(rowMapper.toRow(price)).thenReturn(price);
    }

    @Override
    public Mono<Price> update(Price price) {
        return priceRepository.save(rowMapper.toRow(price)).thenReturn(price);
    }

    @Override
    public Mono<Price> findById(OrganizationId organizationId, PriceId priceId) {
        return priceRepository.findByOrganizationAndId(organizationId.value(), priceId.value()).map(rowMapper::toDomain);
    }

    @Override
    public Flux<Price> findTimeline(OrganizationId organizationId, VariantId variantId, String currency, PriceType priceType) {
        return priceRepository
                .findTimeline(organizationId.value(), variantId.value(), currency, priceType.name())
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Price> resolveActive(OrganizationId organizationId, VariantId variantId, String currency, PriceType priceType, Instant at) {
        return priceRepository
                .resolveActive(organizationId.value(), variantId.value(), currency, priceType.name(), at)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<CatalogOffer> create(CatalogOffer offer) {
        return Mono.just(offer);
    }

    @Override
    public Mono<CatalogOffer> update(CatalogOffer offer) {
        return Mono.just(offer);
    }

    @Override
    public Mono<CatalogOffer> findByOfferId(OrganizationId organizationId, OfferId offerId) {
        return findByVariantId(organizationId, VariantId.of(offerId.value()));
    }

    @Override
    public Mono<CatalogOffer> findByVariantId(OrganizationId organizationId, VariantId variantId) {
        return variantRepository.findByOrganizationAndId(organizationId.value(), variantId.value())
                .switchIfEmpty(Mono.empty())
                .flatMap(variantRow -> productRepository.findByOrganizationAndId(organizationId.value(), variantRow.productId())
                        .zipWith(priceRepository.findLatestByVariant(organizationId.value(), variantRow.variantId()))
                        .map(tuple -> CatalogOffer.rehydrate(
                                OfferId.of(variantId.value()),
                                rowMapper.toDomain(tuple.getT1()),
                                rowMapper.toDomain(variantRow),
                                rowMapper.toDomain(tuple.getT2()),
                                "",
                                variantRow.createdAt(),
                                variantRow.updatedAt())));
    }

    @Override
    public Mono<PriceSchedule> create(PriceSchedule schedule) {
        return entityTemplate.insert(rowMapper.toRow(schedule)).map(rowMapper::toDomain);
    }

    @Override
    public Flux<PriceSchedule> findPending(Instant executeBefore, int limit) {
        return priceScheduleRepository.findPending(executeBefore, limit).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Void> markStatus(String scheduleId, PriceScheduleJobStatus jobStatus, String errorMessage, Instant updatedAt) {
        return priceScheduleRepository.markStatus(scheduleId, jobStatus.name(), errorMessage, updatedAt).then();
    }

    @Override
    public Mono<Void> record(CatalogAuditEntry entry) {
        CatalogAuditRow row = rowMapper.toRow(entry);
        return entityTemplate.insert(row).then();
    }

    @Override
    public Mono<CatalogAuditEntry> findByIdempotency(String organizationId, String actionType, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.empty();
        }
        return catalogAuditRepository.findByIdempotency(organizationId, actionType, idempotencyKey).map(rowMapper::toDomain);
    }

    @Override
    public Flux<CatalogAuditEntry> findByTarget(String organizationId, String targetType, String targetId, int offset, int limit) {
        return catalogAuditRepository.findByTarget(organizationId, emptyAsNull(targetType), emptyAsNull(targetId), offset, limit)
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Long> countByTarget(String organizationId, String targetType, String targetId) {
        return catalogAuditRepository.countByTarget(organizationId, emptyAsNull(targetType), emptyAsNull(targetId));
    }

    @Override
    public Mono<Void> store(DomainEvent event, String payload) {
        OutboxEventRow row = outboxRowMapper.toRow(event, payload);
        return entityTemplate.insert(row).then();
    }

    @Override
    public Flux<PendingOutboxEvent> findPending(int limit) {
        return outboxEventRepository.findPending(limit)
                .map(row -> new PendingOutboxEvent(
                        row.eventId(),
                        row.aggregateType(),
                        row.aggregateId(),
                        row.eventType(),
                        row.payload(),
                        row.retryCount() == null ? 0 : row.retryCount()));
    }

    @Override
    public Mono<Void> markPublished(String eventId, Instant publishedAt) {
        return outboxEventRepository.markPublished(eventId, publishedAt).then();
    }

    @Override
    public Mono<Void> markFailed(String eventId, String errorMessage, Instant updatedAt, int maxRetries) {
        return outboxEventRepository.markFailed(eventId, errorMessage, updatedAt, maxRetries).then();
    }

    @Override
    public Flux<CatalogSearchProjection> search(CatalogSearchFilter filter) {
        Instant at = filter.at() == null ? Instant.now() : filter.at();
        String sql = """
                SELECT p.product_id,
                       p.product_code,
                       p.product_name,
                       v.variant_id,
                       v.sku,
                       v.status AS variant_status,
                       pr.amount,
                       pr.currency,
                       pr.price_type,
                       (v.status = 'SELLABLE' AND pr.price_id IS NOT NULL) AS sellable
                FROM products p
                JOIN variants v ON v.product_id = p.product_id AND v.organization_id = p.organization_id
                LEFT JOIN LATERAL (
                    SELECT pr.*
                    FROM prices pr
                    WHERE pr.organization_id = p.organization_id
                      AND pr.variant_id = v.variant_id
                      AND pr.effective_from <= :at
                      AND (pr.effective_until IS NULL OR pr.effective_until > :at)
                    ORDER BY pr.effective_from DESC
                    LIMIT 1
                ) pr ON TRUE
                WHERE p.organization_id = :organizationId
                  AND (:brandId IS NULL OR p.brand_id = :brandId)
                  AND (:categoryId IS NULL OR p.category_id = :categoryId)
                  AND (:variantStatus IS NULL OR v.status = :variantStatus)
                  AND (:text IS NULL OR UPPER(p.product_name) LIKE UPPER(:textLike)
                                 OR UPPER(p.product_code) LIKE UPPER(:textLike)
                                 OR UPPER(v.sku) LIKE UPPER(:textLike))
                ORDER BY p.updated_at DESC, v.updated_at DESC
                OFFSET :offset
                LIMIT :limit
                """;

        return databaseClient.sql(sql)
                .bind("organizationId", filter.organizationId())
                .bind("brandId", emptyAsNull(filter.brandId()))
                .bind("categoryId", emptyAsNull(filter.categoryId()))
                .bind("variantStatus", emptyAsNull(filter.variantStatus()))
                .bind("text", emptyAsNull(filter.text()))
                .bind("textLike", like(filter.text()))
                .bind("offset", filter.offset())
                .bind("limit", filter.limit())
                .bind("at", at)
                .map((row, metadata) -> new CatalogSearchProjection(
                        row.get("product_id", String.class),
                        row.get("product_code", String.class),
                        row.get("product_name", String.class),
                        row.get("variant_id", String.class),
                        row.get("sku", String.class),
                        row.get("variant_status", String.class),
                        row.get("amount", java.math.BigDecimal.class),
                        row.get("currency", String.class),
                        row.get("price_type", String.class),
                        Boolean.TRUE.equals(row.get("sellable", Boolean.class))))
                .all();
    }

    @Override
    public Mono<Long> count(CatalogSearchFilter filter) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM products p
                JOIN variants v ON v.product_id = p.product_id AND v.organization_id = p.organization_id
                WHERE p.organization_id = :organizationId
                  AND (:brandId IS NULL OR p.brand_id = :brandId)
                  AND (:categoryId IS NULL OR p.category_id = :categoryId)
                  AND (:variantStatus IS NULL OR v.status = :variantStatus)
                  AND (:text IS NULL OR UPPER(p.product_name) LIKE UPPER(:textLike)
                                 OR UPPER(p.product_code) LIKE UPPER(:textLike)
                                 OR UPPER(v.sku) LIKE UPPER(:textLike))
                """;
        return databaseClient.sql(sql)
                .bind("organizationId", filter.organizationId())
                .bind("brandId", emptyAsNull(filter.brandId()))
                .bind("categoryId", emptyAsNull(filter.categoryId()))
                .bind("variantStatus", emptyAsNull(filter.variantStatus()))
                .bind("text", emptyAsNull(filter.text()))
                .bind("textLike", like(filter.text()))
                .map((row, metadata) -> row.get("total", Long.class))
                .one()
                .defaultIfEmpty(0L);
    }

    private Mono<Void> replaceProductTags(String organizationId, String productId, Iterable<ProductTag> tags) {
        List<ProductTag> list = new ArrayList<>();
        for (ProductTag tag : tags) {
            list.add(tag);
        }
        Instant now = Instant.now();
        return productTagRepository
                .deleteByOrganizationAndProduct(organizationId, productId)
                .thenMany(Flux.fromIterable(list)
                        .concatMap(tag -> entityTemplate.insert(rowMapper.toRow(organizationId, productId, tag, now))))
                .then();
    }

    private Mono<Void> replaceVariantAttributes(String organizationId, String variantId, Iterable<VariantAttribute> attributes) {
        List<VariantAttribute> list = new ArrayList<>();
        for (VariantAttribute attribute : attributes) {
            list.add(attribute);
        }
        Instant now = Instant.now();
        return variantAttributeRepository
                .deleteByOrganizationAndVariant(organizationId, variantId)
                .thenMany(Flux.fromIterable(list)
                        .concatMap(attr -> entityTemplate.insert(rowMapper.toRow(organizationId, variantId, attr, now))))
                .then();
    }

    private String emptyAsNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String like(String text) {
        return text == null || text.isBlank() ? null : "%" + text.trim() + "%";
    }
}
