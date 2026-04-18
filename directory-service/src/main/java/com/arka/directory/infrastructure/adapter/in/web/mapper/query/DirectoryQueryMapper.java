package com.arka.directory.infrastructure.adapter.in.web.mapper.query;

import com.arka.directory.application.query.GetActiveCountryPolicyQuery;
import com.arka.directory.application.query.GetDirectoryAdminSummaryQuery;
import com.arka.directory.application.query.GetDirectoryAuditQuery;
import com.arka.directory.application.query.GetOrganizationProfileQuery;
import com.arka.directory.application.query.GetOrganizationQuery;
import com.arka.directory.application.query.ListOrganizationAddressesQuery;
import com.arka.directory.application.query.ListOrganizationContactsQuery;
import com.arka.directory.application.query.ResolveCheckoutAddressQuery;
import com.arka.directory.infrastructure.adapter.in.security.IamSecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
public class DirectoryQueryMapper {

    public GetOrganizationQuery toGetOrganizationQuery(String organizationId, IamSecurityPrincipal principal) {
        return new GetOrganizationQuery(organizationId, principal.userId(), principal.organizationId());
    }

    public GetOrganizationProfileQuery toGetOrganizationProfileQuery(String organizationId, IamSecurityPrincipal principal) {
        return new GetOrganizationProfileQuery(organizationId, principal.userId(), principal.organizationId());
    }

    public ListOrganizationContactsQuery toListOrganizationContactsQuery(String organizationId, IamSecurityPrincipal principal) {
        return new ListOrganizationContactsQuery(organizationId, principal.userId(), principal.organizationId());
    }

    public ListOrganizationAddressesQuery toListOrganizationAddressesQuery(String organizationId, IamSecurityPrincipal principal) {
        return new ListOrganizationAddressesQuery(organizationId, principal.userId(), principal.organizationId());
    }

    public ResolveCheckoutAddressQuery toResolveCheckoutAddressQuery(
            String organizationId,
            String addressId,
            String countryCode,
            IamSecurityPrincipal principal) {
        return new ResolveCheckoutAddressQuery(
                organizationId,
                addressId,
                countryCode,
                principal.userId(),
                principal.organizationId());
    }

    public GetActiveCountryPolicyQuery toGetActiveCountryPolicyQuery(
            String organizationId,
            String countryCode,
            IamSecurityPrincipal principal) {
        return new GetActiveCountryPolicyQuery(
                organizationId,
                countryCode,
                principal.userId(),
                principal.organizationId());
    }

    public GetDirectoryAdminSummaryQuery toGetDirectoryAdminSummaryQuery(IamSecurityPrincipal principal) {
        return new GetDirectoryAdminSummaryQuery(principal.userId());
    }

    public GetDirectoryAuditQuery toGetDirectoryAuditQuery(
            String organizationId,
            int limit,
            IamSecurityPrincipal principal) {
        return new GetDirectoryAuditQuery(organizationId, limit, principal.userId(), principal.organizationId());
    }
}
