package com.arka.catalog.infrastructure.adapter.in.web.mapper.query;

import com.arka.catalog.application.query.GetCatalogAuditQuery;
import com.arka.catalog.application.query.GetPriceTimelineQuery;
import com.arka.catalog.application.query.GetProductByIdQuery;
import com.arka.catalog.application.query.GetProductDetailQuery;
import com.arka.catalog.application.query.ListVariantsByProductQuery;
import com.arka.catalog.application.query.ResolveCurrentPriceQuery;
import com.arka.catalog.application.query.ResolveVariantForCheckoutQuery;
import com.arka.catalog.application.query.SearchCatalogQuery;
import com.arka.catalog.infrastructure.adapter.in.security.IamSecurityPrincipal;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CatalogQueryMapper {

    public GetProductByIdQuery toGetProductByIdQuery(String productId, IamSecurityPrincipal principal) {
        return new GetProductByIdQuery(principal.tenantId(), productId);
    }

    public GetProductDetailQuery toGetProductDetailQuery(String productId, Instant at, IamSecurityPrincipal principal) {
        return new GetProductDetailQuery(principal.tenantId(), productId, at);
    }

    public SearchCatalogQuery toSearchCatalogQuery(
            String text,
            String brandId,
            String categoryId,
            String variantStatus,
            Integer page,
            Integer size,
            Instant at,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new SearchCatalogQuery(
                principal.tenantId(),
                text,
                brandId,
                categoryId,
                variantStatus,
                safePage,
                safeSize,
                at);
    }

    public ListVariantsByProductQuery toListVariantsByProductQuery(String productId, IamSecurityPrincipal principal) {
        return new ListVariantsByProductQuery(principal.tenantId(), productId);
    }

    public ResolveVariantForCheckoutQuery toResolveVariantForCheckoutQuery(
            String sku,
            String currency,
            String priceType,
            Instant at,
            IamSecurityPrincipal principal) {
        return new ResolveVariantForCheckoutQuery(
                principal.tenantId(),
                sku,
                currency,
                priceType,
                at);
    }

    public ResolveCurrentPriceQuery toResolveCurrentPriceQuery(
            String variantId,
            String currency,
            String priceType,
            Instant at,
            IamSecurityPrincipal principal) {
        return new ResolveCurrentPriceQuery(principal.tenantId(), variantId, currency, priceType, at);
    }

    public GetPriceTimelineQuery toGetPriceTimelineQuery(
            String variantId,
            String currency,
            String priceType,
            IamSecurityPrincipal principal) {
        return new GetPriceTimelineQuery(principal.tenantId(), variantId, currency, priceType);
    }

    public GetCatalogAuditQuery toGetCatalogAuditQuery(
            String targetType,
            String targetId,
            Integer page,
            Integer size,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new GetCatalogAuditQuery(principal.tenantId(), targetType, targetId, safePage, safeSize);
    }
}
